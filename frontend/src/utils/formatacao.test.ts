import { describe, expect, it } from 'vitest';
import {
  converterTaxa,
  converterValor,
  formatarData,
  formatarNumero,
  mascararValor,
  sanitizarTaxa,
} from './formatacao';

describe('formatação', () => {
  it('formata números e datas no padrão brasileiro', () => {
    expect(formatarNumero(140791.58)).toBe('140.791,58');
    expect(formatarData('2024-02-29')).toBe('29/02/2024');
  });

  it('aplica máscara monetária tratando os dígitos como centavos', () => {
    expect(mascararValor('14000000')).toBe('140.000,00');
    expect(mascararValor('abc')).toBe('');
    expect(mascararValor('9'.repeat(20))).toBe('9.999.999.999.999,99');
  });

  it('converte o valor mascarado', () => {
    expect(converterValor('140.000,00')).toBe(140000);
    expect(converterValor('1,5')).toBeNull();
  });

  it('sanitiza e converte a taxa', () => {
    expect(sanitizarTaxa('7a,5,3%')).toBe('7,53');
    expect(sanitizarTaxa('12345')).toBe('1234');
    expect(converterTaxa('7,25')).toBe(7.25);
    expect(converterTaxa('7,')).toBeNull();
  });
});
