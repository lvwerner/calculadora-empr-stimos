import { describe, expect, it } from 'vitest';
import type { ValoresFormulario } from '../types';
import { MENSAGENS, paraDadosEmprestimo, validarFormulario } from './validacao';

const VALIDOS: ValoresFormulario = {
  dataInicial: '2024-01-01',
  dataFinal: '2034-01-01',
  primeiroPagamento: '2024-02-15',
  valorEmprestimo: '140.000,00',
  taxaJuros: '7',
};

describe('validarFormulario', () => {
  it('não retorna erros para valores válidos', () => {
    expect(validarFormulario(VALIDOS)).toEqual({});
  });

  it('exige os cinco campos', () => {
    const erros = validarFormulario({
      dataInicial: '',
      dataFinal: '',
      primeiroPagamento: '',
      valorEmprestimo: '',
      taxaJuros: '',
    });
    expect(Object.keys(erros)).toHaveLength(5);
  });

  it('exige data final maior que a data inicial', () => {
    expect(validarFormulario({ ...VALIDOS, dataFinal: '2024-01-01' }).dataFinal).toBe(
      MENSAGENS.dataFinalMenorQueInicial,
    );
  });

  it.each(['2024-01-01', '2034-01-01', '2023-12-31', '2034-02-01'])(
    'rejeita primeiro pagamento fora do período (%s)',
    (primeiroPagamento) => {
      expect(validarFormulario({ ...VALIDOS, primeiroPagamento }).primeiroPagamento).toBe(
        MENSAGENS.primeiroPagamentoForaDoPeriodo,
      );
    },
  );

  it('limita o período máximo', () => {
    expect(validarFormulario({ ...VALIDOS, dataFinal: '2124-01-02' }).dataFinal).toBe(
      MENSAGENS.periodoMuitoLongo,
    );
  });

  it('rejeita datas inexistentes, valor zero e taxa acima do limite', () => {
    const erros = validarFormulario({
      ...VALIDOS,
      dataInicial: '2024-02-30',
      valorEmprestimo: '0,00',
      taxaJuros: '1000,5',
    });
    expect(erros.dataInicial).toBeDefined();
    expect(erros.valorEmprestimo).toBeDefined();
    expect(erros.taxaJuros).toBeDefined();
  });
});

describe('paraDadosEmprestimo', () => {
  it('converte os valores da tela para o corpo da API', () => {
    expect(paraDadosEmprestimo({ ...VALIDOS, taxaJuros: '7,5' })).toEqual({
      dataInicial: '2024-01-01',
      dataFinal: '2034-01-01',
      primeiroPagamento: '2024-02-15',
      valorEmprestimo: 140000,
      taxaJuros: 7.5,
    });
  });
});
