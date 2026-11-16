import { memo, useMemo } from 'react';
import type { LinhaCalculo } from '../types';
import { formatarData, formatarNumero } from '../utils/formatacao';

interface TabelaResultadosProps {
  linhas: LinhaCalculo[];
}

const COLUNAS_NUMERICAS = [
  'valorEmprestimo',
  'saldoDevedor',
  'consolidada',
  'total',
  'amortizacao',
  'saldo',
  'provisao',
  'acumulado',
  'pago',
] as const;

/** Formata as linhas uma única vez por resultado, e não a cada renderização. */
function formatarLinhas(linhas: LinhaCalculo[]) {
  return linhas.map((linha) => ({
    chave: linha.dataCompetencia,
    data: formatarData(linha.dataCompetencia),
    celulas: COLUNAS_NUMERICAS.map((coluna) =>
      coluna === 'consolidada' ? (linha.consolidada ?? '') : formatarNumero(linha[coluna]),
    ),
  }));
}

export const TabelaResultados = memo(function TabelaResultados({ linhas }: TabelaResultadosProps) {
  const linhasFormatadas = useMemo(() => formatarLinhas(linhas), [linhas]);

  return (
    <div className="tabela" tabIndex={0} role="region" aria-labelledby="tabela-titulo">
      <table>
        <caption id="tabela-titulo" className="visualmente-oculto">
          Resultado do cálculo do empréstimo
        </caption>
        <thead>
          <tr>
            <td aria-hidden="true" />
            <th scope="colgroup" colSpan={2}>Empréstimo</th>
            <th scope="colgroup" colSpan={2}>Parcela</th>
            <th scope="colgroup" colSpan={2}>Principal</th>
            <th scope="colgroup" colSpan={3}>Juros</th>
          </tr>
          <tr>
            <th scope="col">Data Competência</th>
            <th scope="col">Valor de Empréstimo</th>
            <th scope="col">Saldo Devedor</th>
            <th scope="col">Consolidada</th>
            <th scope="col">Total</th>
            <th scope="col">Amortização</th>
            <th scope="col">Saldo</th>
            <th scope="col">Provisão</th>
            <th scope="col">Acumulado</th>
            <th scope="col">Pago</th>
          </tr>
        </thead>
        <tbody>
          {linhasFormatadas.map((linha) => (
            <tr key={linha.chave}>
              <th scope="row">{linha.data}</th>
              {linha.celulas.map((celula, indice) => (
                <td key={COLUNAS_NUMERICAS[indice]}>{celula}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
});
