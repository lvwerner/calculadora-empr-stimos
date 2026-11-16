import type { DadosEmprestimo, ErrosFormulario, ValoresFormulario } from '../types';
import { converterTaxa, converterValor } from './formatacao';

/** Mesmo limite de segurança aplicado pela API. */
export const PERIODO_MAXIMO_ANOS = 100;
export const TAXA_MAXIMA = 1000;

export const MENSAGENS = {
  dataFinalMenorQueInicial: 'A data final deve ser maior que a data inicial.',
  primeiroPagamentoForaDoPeriodo:
    'A data de primeiro pagamento deve ser maior que a data inicial e menor que a data final.',
  periodoMuitoLongo: `O período entre a data inicial e a data final não pode exceder ${PERIODO_MAXIMO_ANOS} anos.`,
} as const;

const DATA_ISO = /^\d{4}-\d{2}-\d{2}$/;

function isDataValida(valor: string): boolean {
  if (!DATA_ISO.test(valor)) {
    return false;
  }
  const data = new Date(`${valor}T00:00:00Z`);
  return !Number.isNaN(data.getTime()) && data.toISOString().startsWith(valor);
}

function validarData(valor: string, rotulo: string): string | undefined {
  if (!valor) {
    return `${rotulo} é obrigatória.`;
  }
  return isDataValida(valor) ? undefined : `${rotulo} é inválida.`;
}

function somarAnos(dataIso: string, anos: number): string {
  return `${Number(dataIso.slice(0, 4)) + anos}`.padStart(4, '0') + dataIso.slice(4);
}

/** Datas ISO (aaaa-mm-dd) podem ser comparadas como texto. */
export function validarFormulario(valores: ValoresFormulario): ErrosFormulario {
  const erros: ErrosFormulario = {};

  erros.dataInicial = validarData(valores.dataInicial, 'A data inicial');
  erros.dataFinal = validarData(valores.dataFinal, 'A data final');
  erros.primeiroPagamento = validarData(valores.primeiroPagamento, 'A data do primeiro pagamento');

  if (!erros.dataInicial && !erros.dataFinal) {
    if (valores.dataFinal <= valores.dataInicial) {
      erros.dataFinal = MENSAGENS.dataFinalMenorQueInicial;
    } else if (valores.dataFinal > somarAnos(valores.dataInicial, PERIODO_MAXIMO_ANOS)) {
      erros.dataFinal = MENSAGENS.periodoMuitoLongo;
    }
  }

  if (!erros.primeiroPagamento && !erros.dataInicial && !erros.dataFinal) {
    const { dataInicial, dataFinal, primeiroPagamento } = valores;
    if (primeiroPagamento <= dataInicial || primeiroPagamento >= dataFinal) {
      erros.primeiroPagamento = MENSAGENS.primeiroPagamentoForaDoPeriodo;
    }
  }

  const valor = converterValor(valores.valorEmprestimo);
  if (!valores.valorEmprestimo) {
    erros.valorEmprestimo = 'O valor do empréstimo é obrigatório.';
  } else if (valor === null || valor <= 0) {
    erros.valorEmprestimo = 'O valor do empréstimo deve ser maior que zero.';
  }

  const taxa = converterTaxa(valores.taxaJuros);
  if (!valores.taxaJuros) {
    erros.taxaJuros = 'A taxa de juros é obrigatória.';
  } else if (taxa === null) {
    erros.taxaJuros = 'A taxa de juros é inválida.';
  } else if (taxa > TAXA_MAXIMA) {
    erros.taxaJuros = `A taxa de juros deve ser no máximo ${TAXA_MAXIMA}%.`;
  }

  return Object.fromEntries(
    Object.entries(erros).filter(([, mensagem]) => mensagem !== undefined),
  ) as ErrosFormulario;
}

/** Converte os valores da tela no corpo da API. Use somente após validar. */
export function paraDadosEmprestimo(valores: ValoresFormulario): DadosEmprestimo {
  return {
    dataInicial: valores.dataInicial,
    dataFinal: valores.dataFinal,
    primeiroPagamento: valores.primeiroPagamento,
    valorEmprestimo: converterValor(valores.valorEmprestimo) ?? 0,
    taxaJuros: converterTaxa(valores.taxaJuros) ?? 0,
  };
}
