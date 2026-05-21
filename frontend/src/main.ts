// Punto de entrada: aquí arranca Angular en el navegador
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// Monta el componente raíz App con la configuración de app.config.ts
bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
