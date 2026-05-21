// Cliente HTTP para pagos de eventos (comprobantes, aprobar/rechazar)
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SKIP_GLOBAL_TOAST } from '../interceptors/error.interceptor';
import { Pago } from '../models/domain.models';

@Injectable({ providedIn: 'root' })
export class PagoApi {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl + '/api/pagos';

  // Subir archivo del comprobante de pago
  registrar(eventoId: number, organizadorId: number, archivo: File): Observable<Pago> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    const params = new HttpParams()
      .set('eventoId', String(eventoId))
      .set('organizadorId', String(organizadorId));
    return this.http.post<Pago>(this.base, formData, { params });
  }

  // Admin u organizador aprueba el pago
  aprobar(id: number, aprobadorId?: number): Observable<Pago> {
    const params = aprobadorId
      ? new HttpParams().set('aprobadorId', String(aprobadorId))
      : undefined;
    return this.http.put<Pago>(this.base + '/' + id + '/aprobar', null, { params });
  }

  rechazar(id: number, motivo: string, aprobadorId?: number): Observable<Pago> {
    return this.http.put<Pago>(this.base + '/' + id + '/rechazar', {
      motivo,
      aprobadorId
    });
  }

  // Pagos que faltan por revisar (admin)
  listarPendientes(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.base + '/pendientes');
  }

  listarTodos(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.base);
  }

  listarPorOrganizador(organizadorId: number): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.base + '/organizador/' + organizadorId);
  }

  // Busca el pago de un evento; devuelve null si aún no hay (404 normal)
  obtenerPorEvento(eventoId: number): Observable<Pago | null> {
    return this.http
      .get<Pago>(this.base + '/evento/' + eventoId, {
        headers: new HttpHeaders().set(SKIP_GLOBAL_TOAST, '1')
      })
      .pipe(
        catchError((err) => (err.status === 404 ? of(null) : throwError(() => err)))
      );
  }
}
