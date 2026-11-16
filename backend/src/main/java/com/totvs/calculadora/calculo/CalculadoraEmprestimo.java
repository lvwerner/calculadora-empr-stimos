package com.totvs.calculadora.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gera a grid de cálculo seguindo as fórmulas da planilha "Calculadora_Emprestimos.xlsx".
 * <p>
 * Fórmulas por linha (sufixo {@code ant} = linha anterior):
 * <ul>
 *   <li>Amortização = valor do empréstimo / quantidade de parcelas (somente em data de pagamento)</li>
 *   <li>Saldo = Saldo<sub>ant</sub> - Amortização</li>
 *   <li>Provisão = ((1 + taxa) ^ ((data - data<sub>ant</sub>) / 360) - 1) * (Saldo<sub>ant</sub> + Acumulado<sub>ant</sub>)</li>
 *   <li>Pago = Acumulado<sub>ant</sub> + Provisão (somente em data de pagamento)</li>
 *   <li>Acumulado = Acumulado<sub>ant</sub> + Provisão - Pago</li>
 *   <li>Total = Amortização + Pago</li>
 *   <li>Saldo devedor = Saldo + Acumulado</li>
 * </ul>
 * Os cálculos usam {@code double}, a mesma precisão da planilha (a potência com expoente
 * fracionário não é exata em {@link BigDecimal}). Os valores são arredondados para 2 casas
 * apenas na saída.
 */
public final class CalculadoraEmprestimo {

    /** Base de dias (célula F2 da planilha). */
    static final double BASE_DIAS = 360d;

    private static final int CASAS_DECIMAIS = 2;

    public List<LinhaCalculo> calcular(ParametrosEmprestimo parametros) {
        TreeMap<LocalDate, Boolean> datas = montarDatas(parametros);
        int quantidadeParcelas = (int) datas.values().stream().filter(Boolean::booleanValue).count();

        double valorEmprestimo = parametros.valorEmprestimo().doubleValue();
        double fatorTaxa = 1d + parametros.taxaJuros().doubleValue() / 100d;
        double amortizacaoParcela = valorEmprestimo / quantidadeParcelas;

        List<LinhaCalculo> linhas = new ArrayList<>(datas.size());
        LocalDate dataAnterior = parametros.dataInicial();
        double saldoAnterior = valorEmprestimo;
        double acumuladoAnterior = 0d;
        int parcela = 0;

        linhas.add(new LinhaCalculo(dataAnterior, arredondar(valorEmprestimo), arredondar(valorEmprestimo),
                null, arredondar(0d), arredondar(0d), arredondar(valorEmprestimo),
                arredondar(0d), arredondar(0d), arredondar(0d)));

        for (Map.Entry<LocalDate, Boolean> entrada : datas.tailMap(dataAnterior, false).entrySet()) {
            LocalDate data = entrada.getKey();
            boolean pagamento = entrada.getValue();

            long dias = data.toEpochDay() - dataAnterior.toEpochDay();
            double provisao = (Math.pow(fatorTaxa, dias / BASE_DIAS) - 1d) * (saldoAnterior + acumuladoAnterior);
            double amortizacao = pagamento ? amortizacaoParcela : 0d;
            double pago = pagamento ? acumuladoAnterior + provisao : 0d;
            double saldo = saldoAnterior - amortizacao;
            double acumulado = acumuladoAnterior + provisao - pago;
            String consolidada = pagamento ? (++parcela) + "/" + quantidadeParcelas : null;

            linhas.add(new LinhaCalculo(data, arredondar(0d), arredondar(saldo + acumulado), consolidada,
                    arredondar(amortizacao + pago), arredondar(amortizacao), arredondar(saldo),
                    arredondar(provisao), arredondar(acumulado), arredondar(pago)));

            dataAnterior = data;
            saldoAnterior = saldo;
            acumuladoAnterior = acumulado;
        }
        return linhas;
    }

    /**
     * Monta as datas da grid, ordenadas, indicando se cada uma é data de pagamento:
     * data inicial, fins de mês do período, parcelas mensais no dia do primeiro pagamento
     * (ajustadas para dia útil) e a data final, que sempre é pagamento.
     */
    static TreeMap<LocalDate, Boolean> montarDatas(ParametrosEmprestimo parametros) {
        LocalDate inicio = parametros.dataInicial();
        LocalDate fim = parametros.dataFinal();
        TreeMap<LocalDate, Boolean> datas = new TreeMap<>();
        datas.put(inicio, false);

        for (YearMonth mes = YearMonth.from(inicio); !mes.isAfter(YearMonth.from(fim)); mes = mes.plusMonths(1)) {
            LocalDate fimDoMes = mes.atEndOfMonth();
            if (fimDoMes.isAfter(inicio) && fimDoMes.isBefore(fim)) {
                datas.put(fimDoMes, false);
            }
        }

        CalendarioDiasUteis calendario = CalendarioDiasUteis.paraPeriodo(inicio, fim);
        LocalDate primeiroPagamento = parametros.primeiroPagamento();
        // plusMonths a partir da data original mantém o dia e o limita ao último dia do mês
        // (ex.: 31/01/2024 -> 29/02/2024 -> 31/03/2024).
        for (int i = 0; ; i++) {
            LocalDate pagamento = calendario.proximoDiaUtil(primeiroPagamento.plusMonths(i));
            if (!pagamento.isBefore(fim)) {
                break;
            }
            datas.put(pagamento, true);
        }

        datas.put(fim, true);
        return datas;
    }

    private static BigDecimal arredondar(double valor) {
        return BigDecimal.valueOf(valor).setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP);
    }
}
