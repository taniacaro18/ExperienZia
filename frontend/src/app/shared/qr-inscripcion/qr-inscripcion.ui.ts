// Servicio compartido para mostrar el código QR de una inscripción en un diálogo modal.
import { Injectable, inject, signal } from '@angular/core';
import { MessageService } from 'primeng/api';
import * as QRCode from 'qrcode';

export interface MostrarQrInscripcionOpts {
  codigo?: string | null;
  titulo?: string;
  nombreArchivo?: string;
}

@Injectable({ providedIn: 'root' })
export class QrInscripcionUi {
  private readonly messages = inject(MessageService);

  readonly visible = signal(false);
  readonly titulo = signal('Tu código QR');
  readonly codigo = signal<string | null>(null);
  readonly qrDataUrl = signal<string | null>(null);
  private readonly nombreArchivo = signal('evento');

  async mostrar(opts: MostrarQrInscripcionOpts): Promise<void> {
    const codigo = opts.codigo?.trim();
    if (!codigo) {
      this.messages.add({
        severity: 'warn',
        summary: 'Sin QR',
        detail: 'Esta inscripción aún no tiene código QR.'
      });
      return;
    }
    try {
      const dataUrl = await QRCode.toDataURL(codigo, {
        width: 360,
        margin: 1,
        color: { dark: '#6D28D9', light: '#FFFFFF' }
      });
      this.titulo.set(opts.titulo ?? 'Tu código QR');
      this.codigo.set(codigo);
      this.nombreArchivo.set(opts.nombreArchivo ?? 'evento');
      this.qrDataUrl.set(dataUrl);
      this.visible.set(true);
    } catch {
      this.messages.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo generar el código QR.'
      });
    }
  }

  cerrar(): void {
    this.visible.set(false);
    this.codigo.set(null);
    this.qrDataUrl.set(null);
  }

  descargar(): void {
    const dataUrl = this.qrDataUrl();
    if (!dataUrl) return;
    const a = document.createElement('a');
    a.href = dataUrl;
    a.download = `${this.nombreArchivo()}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}
