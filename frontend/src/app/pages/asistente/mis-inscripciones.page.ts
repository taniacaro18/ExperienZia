// Archivo `pages/asistente/mis-inscripciones.page.ts` — pages: mis inscripciones.
import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { InscripcionApi } from '../../core/api/inscripcion.api';
import { EventoApi } from '../../core/api/evento.api';
import { AuthStore } from '../../core/auth/auth.store';
import { Evento, Inscripcion } from '../../core/models/domain.models';
import { AforoBarComponent } from '../../shared/aforo-bar/aforo-bar.component';
import { eventoVentanaYaCerro } from '../../shared/evento-catalogo.helpers';
import { forkJoin } from 'rxjs';
import { QrInscripcionUi } from '../../shared/qr-inscripcion/qr-inscripcion.ui';

interface InscripcionConEvento {
  inscripcion: Inscripcion;
  evento: Evento | null;
}

@Component({
  selector: 'app-mis-inscripciones-page',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    RouterLink,
    ProgressSpinnerModule,
    ConfirmDialogModule,
    AforoBarComponent
  ],
  templateUrl: './mis-inscripciones.page.html',
  styleUrl: './mis-inscripciones.page.scss'
})
export class MisInscripcionesPage {
  private readonly inscripcionesApi = inject(InscripcionApi);
  private readonly eventosApi = inject(EventoApi);
  private readonly auth = inject(AuthStore);
  private readonly messages = inject(MessageService);
  private readonly confirma = inject(ConfirmationService);
  private readonly qrUi = inject(QrInscripcionUi);

  readonly cargando = signal(true);
  readonly items = signal<InscripcionConEvento[]>([]);

  readonly proximas = computed(() =>
    this.items().filter(
      (i) =>
        i.evento &&
        i.inscripcion.estado !== 'CANCELADO' &&
        !eventoVentanaYaCerro(i.evento)
    )
  );

  readonly pasadas = computed(() =>
    this.items().filter(
      (i) =>
        !i.evento ||
        i.inscripcion.estado === 'CANCELADO' ||
        (i.evento != null && eventoVentanaYaCerro(i.evento))
    )
  );

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    const u = this.auth.usuario();
    if (!u) return;
    this.cargando.set(true);
    this.inscripcionesApi.listarPorUsuario(u.id).subscribe({
      next: (lista) => {
        if (lista.length === 0) {
          this.items.set([]);
          this.cargando.set(false);
          return;
        }
        const calls = lista.map((ins) => this.eventosApi.obtener(ins.eventoId));
        forkJoin(calls).subscribe({
          next: (eventos) => {
            const combinados = lista.map((inscripcion, i) => ({
              inscripcion,
              evento: eventos[i] ?? null
            }));
            const prioridadEstado = (est: Inscripcion['estado']): number => {
              if (est === 'CANCELADO') return 0;
              if (est === 'INSCRITO') return 2;
              if (est === 'ASISTIO') return 1;
              return 0;
            };
            const dedupePorEvento = (rows: InscripcionConEvento[]): InscripcionConEvento[] => {
              const porEvento = new Map<number, InscripcionConEvento>();
              for (const row of rows) {
                const eid = row.inscripcion.eventoId;
                const prev = porEvento.get(eid);
                if (!prev) {
                  porEvento.set(eid, row);
                  continue;
                }
                const pr = prioridadEstado(row.inscripcion.estado);
                const pp = prioridadEstado(prev.inscripcion.estado);
                if (pr > pp) porEvento.set(eid, row);
                else if (pr === pp && row.inscripcion.id > prev.inscripcion.id) porEvento.set(eid, row);
              }
              return [...porEvento.values()];
            };
            const sinDuplicados = dedupePorEvento(combinados);
            sinDuplicados.sort((a, b) => {
              const fa = a.evento ? new Date(a.evento.fecha).getTime() : 0;
              const fb = b.evento ? new Date(b.evento.fecha).getTime() : 0;
              return fb - fa;
            });
            this.items.set(sinDuplicados);
            this.cargando.set(false);
          },
          error: () => this.cargando.set(false)
        });
      },
      error: () => this.cargando.set(false)
    });
  }

  inicialEvento(item: InscripcionConEvento): string {
    const n = (item.evento?.nombre ?? '?').trim();
    return n ? n.charAt(0).toUpperCase() : '?';
  }

  verQR(item: InscripcionConEvento): void {
    void this.qrUi.mostrar({
      codigo: item.inscripcion.codigoQR,
      titulo: item.evento?.nombre ?? 'Tu código QR',
      nombreArchivo: `qr-${item.evento?.nombre ?? 'evento'}-${item.inscripcion.id}`
    });
  }

  cancelar(item: InscripcionConEvento) {
    this.confirma.confirm({
      message: '¿Cancelar tu inscripción a "' + (item.evento?.nombre ?? 'este evento') + '"?',
      header: 'Confirmar',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, cancelar',
      rejectLabel: 'No',
      acceptButtonStyleClass: '!bg-red-600 !border-red-600',
      accept: () => {
        this.inscripcionesApi.cancelarInscripcion(item.inscripcion.id).subscribe({
          next: () => {
            this.messages.add({
              severity: 'success',
              summary: 'Inscripción cancelada',
              detail: 'Tu cupo fue liberado.'
            });
            this.cargar();
          }
        });
      }
    });
  }

}
