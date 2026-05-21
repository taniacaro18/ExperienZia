// Variables para producción (build con ng build --configuration production)
export const environment = {
  production: true, // true = app publicada en servidor real
  apiUrl: '/api', // el proxy del servidor redirige al backend
  appName: 'ExperienZia',
  storageKey: 'experienzia.session'
};
