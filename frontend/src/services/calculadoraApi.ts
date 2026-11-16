import type { DadosEmprestimo, LinhaCalculo } from '../types';

const URL_CALCULO = '/api/emprestimos/calculos';
const MENSAGEM_PADRAO = 'Não foi possível calcular. Tente novamente.';

interface ProblemDetail {
  detail?: string;
  erros?: { campo: string; mensagem: string }[];
}

export class ErroCalculo extends Error {}

export async function calcularEmprestimo(
  dados: DadosEmprestimo,
  signal?: AbortSignal,
): Promise<LinhaCalculo[]> {
  const resposta = await fetch(URL_CALCULO, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(dados),
    credentials: 'same-origin',
    signal,
  });

  if (!resposta.ok) {
    throw new ErroCalculo(await extrairMensagem(resposta));
  }
  return (await resposta.json()) as LinhaCalculo[];
}

async function extrairMensagem(resposta: Response): Promise<string> {
  if (resposta.status !== 400) {
    return MENSAGEM_PADRAO;
  }
  try {
    const problema = (await resposta.json()) as ProblemDetail;
    const mensagens = problema.erros?.map((erro) => erro.mensagem);
    return mensagens?.length ? mensagens.join(' ') : (problema.detail ?? MENSAGEM_PADRAO);
  } catch {
    return MENSAGEM_PADRAO;
  }
}
