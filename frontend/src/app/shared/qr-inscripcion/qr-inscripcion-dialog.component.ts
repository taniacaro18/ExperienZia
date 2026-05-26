// Diálogo modal reutilizable para el código QR de inscripción (asistente).
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { QrInscripcionUi } from './qr-inscripcion.ui';

@Component({
  selector: 'app-qr-inscripcion-dialog',
  standalone: true,
  imports: [CommonModule, DialogModule],
  templateUrl: './qr-inscripcion-dialog.component.html'
})
export class QrInscripcionDialogComponent {
  readonly ui = inject(QrInscripcionUi);
}
