// Cliente HTTP para certificados de asistencia
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Certificado } from '../models/domain.models';

@Injectable({ providedIn: 'root' })
export class CertificadoApi {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl + '/api/certificados';

  // Generar certificado para una inscripción
  generar(inscripcionId: number): Observable<Certificado> {
    return this.http.post<Certificado>(this.base + '/generar/' + inscripcionId, null);
  }

  listarPorUsuario(usuarioId: number): Observable<Certificado[]> {
    return this.http.get<Certificado[]>(this.base + '/usuario/' + usuarioId);
  }

  // Comprobar si un código de certificado es válido
  validar(codigo: string): Observable<Certificado> {
    return this.http.get<Certificado>(this.base + '/validar/' + codigo);
  }

  // Crear certificados para todos los que asistieron al evento
  generarMasivo(eventoId: number, organizadorId?: number): Observable<Certificado[]> {
    const params = organizadorId
      ? new HttpParams().set('organizadorId', String(organizadorId))
      : undefined;
    return this.http.post<Certificado[]>(
      this.base + '/evento/' + eventoId + '/generar-masivo',
      null,
      { params }
    );
  }

  listarPorEvento(eventoId: number): Observable<Certificado[]> {
    return this.http.get<Certificado[]>(this.base + '/evento/' + eventoId);
  }
}
