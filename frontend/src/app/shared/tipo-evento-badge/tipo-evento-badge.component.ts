// Badge lila/gris para que en tablas se vea rápido si el evento es público o privado
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TipoEvento } from '../../core/models/domain.models';

@Component({
  selector: 'app-tipo-evento-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span
      class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wide border"
      [ngClass]="tipo === 'PUBLICO'
        ? 'bg-brand-100 text-brand-800 border-brand-200'
        : 'bg-surface-100 text-surface-600 border-surface-200'"
    >
      {{ tipo === 'PUBLICO' ? 'Público' : 'Privado' }}
    </span>
  `
})
export class TipoEventoBadgeComponent {
  @Input({ required: true }) tipo!: TipoEvento;
}
