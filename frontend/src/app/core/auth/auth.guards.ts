// Guards de rutas: bloquean o permiten entrar a una página según el login y el rol
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../models/domain.models';
import { AuthStore } from './auth.store';

// Solo deja pasar si ya iniciaste sesión; si no, te manda al login
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthStore);
  const router = inject(Router);
  if (auth.autenticado()) return true;
  router.navigate(['/login']);
  return false;
};

// Solo deja pasar si tu rol está en la lista (ej. solo ORGANIZADOR)
export const rolGuard = (rolesPermitidos: Rol[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthStore);
    const router = inject(Router);
    const rol = auth.rol();
    if (rol && rolesPermitidos.includes(rol)) return true;
    router.navigate(['/login']);
    return false;
  };
};

// Para login/registro: si ya estás logueado te manda al inicio
export const noAuthGuard: CanActivateFn = () => {
  const auth = inject(AuthStore);
  const router = inject(Router);
  if (!auth.autenticado()) return true;
  router.navigate(['/']);
  return false;
};
