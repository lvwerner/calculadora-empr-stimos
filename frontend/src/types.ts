export type CampoFormulario =
  | 'dataInicial'
  | 'dataFinal'
  | 'primeiroPagamento'
  | 'valorEmprestimo'
  | 'taxaJuros';

/** Valores digitados na tela (datas no formato ISO aaaa-mm-dd). */
export type ValoresFormulario = Record<CampoFormulario, string>;

export type ErrosFormulario = Partial<Record<CampoFormulario, string>>;

/** Corpo enviado para a API. A taxa de juros é percentual (7 = 7%). */
export interface DadosEmprestimo {
  dataInicial: string;
  dataFinal: string;
  primeiroPagamento: string;
  valorEmprestimo: number;
  taxaJuros: number;
}

/** Linha da grid devolvida pela API. */
export interface LinhaCalculo {
  dataCompetencia: string;
  valorEmprestimo: number;
  saldoDevedor: number;
  consolidada: string | null;
  total: number;
  amortizacao: number;
  saldo: number;
  provisao: number;
  acumulado: number;
  pago: number;
}
