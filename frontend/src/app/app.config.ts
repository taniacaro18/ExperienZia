// Configuración global de la app Angular (proveedores que usa toda la aplicación)
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection
} from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { ExperienziaPreset } from './theme/experienzia-preset';

// Aquí registramos todo lo que Angular necesita al arrancar
export const appConfig: ApplicationConfig = {
  providers: [
    // Escucha errores globales del navegador
    provideBrowserGlobalErrorListeners(),
    // Zone.js para detectar cambios en la pantalla
    provideZoneChangeDetection({ eventCoalescing: true }),
    // El router con nuestras rutas (app.routes.ts)
    provideRouter(routes, withComponentInputBinding()),
    // HttpClient con interceptores: token JWT y mensajes de error
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    // Animaciones de PrimeNG
    provideAnimationsAsync(),
    // Tema visual de PrimeNG (colores ExperienZia)
    providePrimeNG({
      theme: {
        preset: ExperienziaPreset,
        options: {
          prefix: 'p',
          darkModeSelector: '[data-theme="dark"]',
          cssLayer: {
            name: 'primeng',
            order: 'tailwind-base, primeng, tailwind-utilities'
          }
        }
      },
      ripple: true
    }),
    // Servicios de PrimeNG para toasts y diálogos de confirmación
    MessageService,
    ConfirmationService
  ]
};
