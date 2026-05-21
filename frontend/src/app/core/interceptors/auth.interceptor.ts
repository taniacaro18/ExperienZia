// Interceptor HTTP: pone el token JWT en cada petición al backend
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStore } from '../auth/auth.store';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(AuthStore);
  const token = store.accessToken();
  // Sin token no modificamos la petición (rutas públicas)
  if (!token) {
    return next(req);
  }
  // Clonamos la petición y añadimos el header Authorization
  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    })
  );
};
