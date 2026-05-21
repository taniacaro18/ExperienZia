// Componente raíz de la aplicación (envuelve todas las páginas)
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@Component({
  // Selector que va en index.html: <app-root>
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ToastModule, ConfirmDialogModule],
  template: `
    <!-- Mensajes tipo toast arriba a la derecha -->
    <p-toast position="top-right"></p-toast>
    <!-- Ventanas de "¿Estás seguro?" -->
    <p-confirmDialog></p-confirmDialog>
    <div class="app-router-host">
      <!-- Aquí se muestra la página según la ruta actual -->
      <router-outlet></router-outlet>
    </div>
  `
})
// Clase vacía porque todo el HTML está en el template de arriba
export class App {}
