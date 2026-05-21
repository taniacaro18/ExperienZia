// Definición de todas las URLs de la aplicación
import { Routes } from '@angular/router';
import { authGuard, noAuthGuard, rolGuard } from './core/auth/auth.guards';

export const routes: Routes = [
  // --- Rutas públicas sin login ---
  {
    // Catálogo de eventos que cualquiera puede ver
    path: 'catalogo',
    loadComponent: () =>
      import('./pages/public/catalogo-publico.page').then((m) => m.CatalogoPublicoPage)
  },
  {
    // Detalle de un evento en el catálogo público
    path: 'catalogo/:id',
    loadComponent: () =>
      import('./pages/public/detalle-evento-publico.page').then((m) => m.DetalleEventoPublicoPage)
  },
  // Login y registro: solo si NO estás logueado (noAuthGuard)
  {
    path: 'login',
    canActivate: [noAuthGuard],
    loadComponent: () => import('./pages/auth/login.page').then((m) => m.LoginPage)
  },
  {
    // Crear cuenta nueva
    path: 'registro',
    canActivate: [noAuthGuard],
    loadComponent: () => import('./pages/auth/registro.page').then((m) => m.RegistroPage)
  },

  // Zona privada: hace falta estar logueado (authGuard) y usa el menú lateral (shell)
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/shell/shell-layout.component').then((m) => m.ShellLayoutComponent),
    children: [
      // Si entras a "/" te manda a inicio
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      {
        // Página principal después del login
        path: 'inicio',
        loadComponent: () => import('./pages/inicio/inicio.page').then((m) => m.InicioPage)
      },
      {
        // Editar datos del usuario logueado
        path: 'perfil',
        loadComponent: () => import('./pages/common/perfil.page').then((m) => m.PerfilPage)
      },
      {
        // Bandeja de notificaciones in-app, disponible para todos los roles.
        path: 'notificaciones',
        loadComponent: () =>
          import('./pages/common/notificaciones.page').then((m) => m.NotificacionesPage)
      },

      // ----- Rutas de ASISTENTE (y admin puede ver catálogo interno) -----
      {
        // Lista de eventos para inscribirse (rol ASISTENTE o ADMIN)
        path: 'eventos',
        canActivate: [rolGuard(['ASISTENTE', 'ADMIN'])],
        loadComponent: () =>
          import('./pages/asistente/eventos-catalogo.page').then((m) => m.EventosCatalogoPage)
      },
      {
        // El detalle de evento también lo abren organizadores (para ver su propio
        // evento desde su lista) y staff (para ver eventos asignados). El propio
        // componente decide qué acciones mostrar según el rol y la propiedad.
        path: 'eventos/:id',
        canActivate: [rolGuard(['ASISTENTE', 'ADMIN', 'ORGANIZADOR', 'STAFF'])],
        loadComponent: () =>
          import('./pages/asistente/evento-detalle.page').then((m) => m.EventoDetallePage)
      },
      {
        // Mis eventos a los que me inscribí
        path: 'mis-inscripciones',
        canActivate: [rolGuard(['ASISTENTE'])],
        loadComponent: () =>
          import('./pages/asistente/mis-inscripciones.page').then((m) => m.MisInscripcionesPage)
      },
      {
        // Certificados que ya me generaron
        path: 'mis-certificados',
        canActivate: [rolGuard(['ASISTENTE'])],
        loadComponent: () =>
          import('./pages/asistente/mis-certificados.page').then((m) => m.MisCertificadosPage)
      },

      // ----- Rutas de ORGANIZADOR (solo rol ORGANIZADOR) -----
      {
        // Panel resumen del organizador
        path: 'organizador/dashboard',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/dashboard.page').then((m) => m.OrgDashboardPage)
      },
      {
        // Lista de mis eventos creados
        path: 'organizador/eventos',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/eventos-lista.page').then((m) => m.OrgEventosListaPage)
      },
      {
        // Formulario para crear evento nuevo
        path: 'organizador/eventos/nuevo',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/evento-form.page').then((m) => m.OrgEventoFormPage)
      },
      {
        // Editar un evento que ya existe
        path: 'organizador/eventos/:id/editar',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/evento-form.page').then((m) => m.OrgEventoFormPage)
      },
      {
        // Gestionar asistentes de mis eventos
        path: 'organizador/asistentes',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/asistentes.page').then((m) => m.OrgAsistentesPage)
      },
      {
        // Crear y administrar personal (staff)
        path: 'organizador/staff',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/staff.page').then((m) => m.OrgStaffPage)
      },
      {
        // Subir comprobantes de pago de eventos
        path: 'organizador/pagos',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/pagos.page').then((m) => m.OrgPagosPage)
      },
      {
        // Reportes y estadísticas de mis eventos
        path: 'organizador/reportes',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/reportes.page').then((m) => m.OrgReportesPage)
      },
      {
        // Generar certificados masivos por evento
        path: 'organizador/certificados',
        canActivate: [rolGuard(['ORGANIZADOR'])],
        loadComponent: () =>
          import('./pages/organizador/certificados.page').then((m) => m.OrgCertificadosPage)
      },

      // ----- Rutas de STAFF (personal en puerta / check-in) -----
      {
        // Inicio del staff
        path: 'staff/dashboard',
        canActivate: [rolGuard(['STAFF'])],
        loadComponent: () =>
          import('./pages/staff/dashboard.page').then((m) => m.StaffDashboardPage)
      },
      {
        // Eventos donde estoy asignado
        path: 'staff/eventos',
        canActivate: [rolGuard(['STAFF'])],
        loadComponent: () =>
          import('./pages/staff/eventos.page').then((m) => m.StaffEventosPage)
      },
      {
        // Detalle de un evento para el staff
        path: 'staff/eventos/:id',
        canActivate: [rolGuard(['STAFF'])],
        loadComponent: () =>
          import('./pages/staff/evento-detalle.page').then((m) => m.StaffEventoDetallePage)
      },
      {
        // Escanear QR para check-in / check-out
        path: 'staff/qr',
        canActivate: [rolGuard(['STAFF'])],
        loadComponent: () =>
          import('./pages/staff/validador-qr.page').then((m) => m.StaffValidadorQrPage)
      },

      // ----- Rutas de ADMIN (administrador del sistema) -----
      {
        // Panel general del administrador
        path: 'admin/dashboard',
        canActivate: [rolGuard(['ADMIN'])],
        loadComponent: () =>
          import('./pages/admin/dashboard.page').then((m) => m.AdminDashboardPage)
      },
      {
        // Aprobar o rechazar eventos de todos
        path: 'admin/eventos',
        canActivate: [rolGuard(['ADMIN'])],
        loadComponent: () =>
          import('./pages/admin/eventos.page').then((m) => m.AdminEventosPage)
      },
      {
        // Gestionar usuarios y organizadores
        path: 'admin/usuarios',
        canActivate: [rolGuard(['ADMIN'])],
        loadComponent: () =>
          import('./pages/admin/usuarios.page').then((m) => m.AdminUsuariosPage)
      },
      {
        // Aprobar pagos pendientes
        path: 'admin/pagos',
        canActivate: [rolGuard(['ADMIN'])],
        loadComponent: () =>
          import('./pages/admin/pagos.page').then((m) => m.AdminPagosPage)
      },
      {
        // Reportes globales de la plataforma
        path: 'admin/reportes',
        canActivate: [rolGuard(['ADMIN'])],
        loadComponent: () =>
          import('./pages/admin/reportes.page').then((m) => m.AdminReportesPage)
      },
      {
        // Historial de acciones (auditoría)
        path: 'admin/auditoria',
        canActivate: [rolGuard(['ADMIN'])],
        loadComponent: () =>
          import('./pages/admin/auditoria.page').then((m) => m.AdminAuditoriaPage)
      }
    ]
  },
  // Cualquier URL que no exista vuelve al catálogo público
  { path: '**', redirectTo: 'catalogo' }
];
