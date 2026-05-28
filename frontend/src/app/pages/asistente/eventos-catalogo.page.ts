// Archivo `pages/asistente/eventos-catalogo.page.ts` — pages: eventos catalogo.
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { EventoApi, EventoSearchCriteria } from '../../core/api/evento.api';
import { Evento } from '../../core/models/domain.models';
import { TipoEventoBadgeComponent } from '../../shared/tipo-evento-badge/tipo-evento-badge.component';

@Component({
  selector: 'app-eventos-catalogo-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DatePipe,
    ButtonModule,
    InputTextModule,
    SelectModule,
    DatePickerModule,
    TagModule,
    CardModule,
    ProgressSpinnerModule,
    TipoEventoBadgeComponent
  ],
  templateUrl: './eventos-catalogo.page.html'
})
export class EventosCatalogoPage implements OnInit {
  private readonly api = inject(EventoApi);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly cargando = signal(true);
  readonly eventos = signal<Evento[]>([]);

  readonly filtros = this.fb.group({
    nombre: [''],
    categoria: [''],
    fechaDesde: [null as Date | null],
    fechaHasta: [null as Date | null]
  });

  readonly opcionesCategoria = computed(() => {
    const set = new Set<string>();
    for (const e of this.eventos()) {
      const c = e.categoria?.trim();
      if (c) set.add(c);
    }
    return [
      { label: 'Todas las categorías', value: '' },
      ...Array.from(set)
        .sort((a, b) => a.localeCompare(b))
        .map((c) => ({ label: c, value: c }))
    ];
  });

  readonly eventosFiltrados = computed(() => this.eventos());

  ngOnInit() {
    // Filtro al tiro: cada cambio en el form dispara búsqueda sin botón "Buscar"
    this.filtros.valueChanges
      .pipe(debounceTime(250), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.buscar());

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const q = params.get('q')?.trim();
      if (q) {
        this.filtros.patchValue({ nombre: q }, { emitEvent: false });
      }
      this.buscar();
    });
  }

  buscar() {
    this.cargando.set(true);
    const v = this.filtros.value;
    const criterios: EventoSearchCriteria = {
      nombre: v.nombre || undefined,
      categoria: v.categoria || undefined,
      estado: 'ACTIVO',
      fechaDesde: v.fechaDesde ? this.toIsoString(v.fechaDesde) : undefined,
      fechaHasta: v.fechaHasta ? this.toIsoString(v.fechaHasta) : undefined
    };
    this.api.buscar(criterios).subscribe({
      next: (lista) => {
        this.eventos.set(lista.filter((e) => e.tipoEvento === 'PUBLICO'));
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  limpiarFiltros() {
    this.filtros.reset({
      nombre: '',
      categoria: '',
      fechaDesde: null,
      fechaHasta: null
    });
  }

  abrir(e: Evento) {
    this.router.navigate(['/eventos', e.id], { queryParams: { retorno: 'eventos' } });
  }

  cuposDisponibles(e: Evento): number {
    return Math.max(0, e.aforoMaximo - e.aforoActual);
  }

  porcentajeOcupacion(e: Evento): number {
    if (!e.aforoMaximo) return 0;
    return Math.min(100, Math.round((e.aforoActual / e.aforoMaximo) * 100));
  }

  private toIsoString(d: Date): string {
    return new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0, 0, 0).toISOString();
  }
}
