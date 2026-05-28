// Descargas de PDF/Excel generados en el backend (con logo y diseño ejecutivo)
import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ExportApi {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl + '/api/export';

  descargarReportePagosAdminPdf(): Observable<Blob> {
    return this.descargarArchivo(this.base + '/admin/reportes/pagos', 'reporte_pagos_experienzia.pdf');
  }

  descargarReporteUsuariosAdminPdf(): Observable<Blob> {
    return this.descargarArchivo(this.base + '/admin/reportes/usuarios', 'reporte_usuarios_experienzia.pdf');
  }

  private descargarArchivo(url: string, filename: string): Observable<Blob> {
    return this.http.get(url, {
      responseType: 'blob',
      headers: { Accept: 'application/pdf' }
    }).pipe(
      tap((blob) => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);
      })
    );
  }
}
