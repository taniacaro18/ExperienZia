// Store de sesión: guarda quién está logueado y el token JWT
import { Injectable, computed, effect, signal } from '@angular/core';
import { Usuario } from '../models/domain.models';
import { environment } from '../../../environments/environment';

interface SesionAlmacenada {
  usuario: Usuario;
  accessToken: string;
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
  // Al abrir la app intentamos recuperar la sesión del localStorage
  private readonly inicial = this.cargarSesion();
  private readonly _usuario = signal<Usuario | null>(this.inicial?.usuario ?? null);
  private readonly _accessToken = signal<string | null>(this.inicial?.accessToken ?? null);

  // Señales de solo lectura para usar en componentes y guards
  readonly usuario = this._usuario.asReadonly();
  readonly accessToken = this._accessToken.asReadonly();
  // true si hay usuario Y token
  readonly autenticado = computed(
    () => this._usuario() !== null && this._accessToken() !== null
  );
  readonly rol = computed(() => this._usuario()?.rol ?? null);
  // Atajos para saber el rol sin comparar strings en cada pantalla
  readonly esAdmin = computed(() => this.rol() === 'ADMIN');
  readonly esOrganizador = computed(() => this.rol() === 'ORGANIZADOR');
  readonly esAsistente = computed(() => this.rol() === 'ASISTENTE');
  readonly esStaff = computed(() => this.rol() === 'STAFF');

  constructor() {
    // Cada vez que cambia usuario o token, guardamos o borramos en localStorage
    effect(() => {
      const u = this._usuario();
      const t = this._accessToken();
      if (u && t) {
        const payload: SesionAlmacenada = { usuario: u, accessToken: t };
        localStorage.setItem(environment.storageKey, JSON.stringify(payload));
      } else {
        localStorage.removeItem(environment.storageKey);
      }
    });
  }

  // Llamar después del login exitoso
  setSesion(usuario: Usuario, accessToken: string) {
    this._usuario.set(usuario);
    this._accessToken.set(accessToken);
  }

  /** Actualiza el usuario logueado (p. ej. tras editar perfil); mantiene el token. */
  setUsuario(u: Usuario | null) {
    if (u === null) {
      this.logout();
      return;
    }
    this._usuario.set(u);
  }

  // Borrar sesión (usuario y token a null)
  logout() {
    this._usuario.set(null);
    this._accessToken.set(null);
  }

  // Lee la sesión guardada al recargar la página
  private cargarSesion(): SesionAlmacenada | null {
    try {
      const raw = localStorage.getItem(environment.storageKey);
      if (!raw) {
        return null;
      }
      const parsed = JSON.parse(raw) as unknown;
      if (
        parsed &&
        typeof parsed === 'object' &&
        'accessToken' in parsed &&
        'usuario' in parsed
      ) {
        const p = parsed as SesionAlmacenada;
        if (p.accessToken && p.usuario) {
          return p;
        }
      }
      // Formato antiguo (solo usuario): obligar a iniciar sesión de nuevo
      localStorage.removeItem(environment.storageKey);
      return null;
    } catch {
      localStorage.removeItem(environment.storageKey);
      return null;
    }
  }
}
