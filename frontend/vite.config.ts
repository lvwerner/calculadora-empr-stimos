import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

const API_URL = process.env.API_URL ?? 'http://localhost:8080';

// A API é acessada pela mesma origem (proxy), sem necessidade de liberar CORS no back-end.
const proxy = { '/api': { target: API_URL, changeOrigin: true } };

const cabecalhosSeguranca = {
  'Content-Security-Policy':
    "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self'; " +
    "object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'",
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'DENY',
  'Referrer-Policy': 'no-referrer',
};

export default defineConfig({
  plugins: [react()],
  server: { proxy },
  preview: { proxy, headers: cabecalhosSeguranca },
  build: { sourcemap: false },
  test: { environment: 'node' },
});
