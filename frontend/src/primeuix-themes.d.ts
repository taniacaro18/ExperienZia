// PrimeUIX no trae tipos publicados; esto evita TS7016 en strict mode.
declare module '@primeuix/themes' {
  export function definePreset(base: unknown, config: unknown): unknown;
}

declare module '@primeuix/themes/aura' {
  const Aura: unknown;
  export default Aura;
}
