const formatoNumero = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/** Máximo de dígitos do valor: 13 inteiros + 2 decimais (mesmo limite da API). */
const MAXIMO_DIGITOS_VALOR = 15;

export function formatarNumero(valor: number): string {
  return formatoNumero.format(valor);
}

/** Converte aaaa-mm-dd em dd/mm/aaaa sem passar por Date (evita erro de fuso horário). */
export function formatarData(dataIso: string): string {
  const [ano, mes, dia] = dataIso.split('-');
  return `${dia}/${mes}/${ano}`;
}

/** Máscara monetária: os dígitos digitados são tratados como centavos ("14000000" -> "140.000,00"). */
export function mascararValor(entrada: string): string {
  const digitos = entrada.replace(/\D/g, '').replace(/^0+/, '').slice(0, MAXIMO_DIGITOS_VALOR);
  return digitos ? formatarNumero(Number(digitos) / 100) : '';
}

/** "140.000,00" -> 140000. Retorna null se vazio ou inválido. */
export function converterValor(texto: string): number | null {
  if (!/^\d{1,3}(\.\d{3})*,\d{2}$/.test(texto)) {
    return null;
  }
  return Number(texto.replace(/\./g, '').replace(',', '.'));
}

/** Mantém apenas dígitos e uma vírgula, com até 4 casas decimais. */
export function sanitizarTaxa(entrada: string): string {
  const [inteiro, ...resto] = entrada.replace(/[^\d,]/g, '').split(',');
  const inteiroLimitado = inteiro.slice(0, 4);
  return resto.length ? `${inteiroLimitado},${resto.join('').slice(0, 4)}` : inteiroLimitado;
}

/** "7,5" -> 7.5. Retorna null se vazio ou inválido. */
export function converterTaxa(texto: string): number | null {
  if (!/^\d{1,4}(,\d{1,4})?$/.test(texto)) {
    return null;
  }
  return Number(texto.replace(',', '.'));
}
