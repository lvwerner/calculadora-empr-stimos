package com.totvs.calculadora.calculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Parâmetros de entrada do cálculo.
 *
 * @param taxaJuros taxa anual em percentual (ex.: 7 para 7%)
 */
public record ParametrosEmprestimo(
        LocalDate dataInicial,
        LocalDate dataFinal,
        LocalDate primeiroPagamento,
        BigDecimal valorEmprestimo,
        BigDecimal taxaJuros) {

    public ParametrosEmprestimo {
        Objects.requireNonNull(dataInicial, "dataInicial");
        Objects.requireNonNull(dataFinal, "dataFinal");
        Objects.requireNonNull(primeiroPagamento, "primeiroPagamento");
        Objects.requireNonNull(valorEmprestimo, "valorEmprestimo");
        Objects.requireNonNull(taxaJuros, "taxaJuros");
        if (!dataFinal.isAfter(dataInicial)) {
            throw new IllegalArgumentException("A data final deve ser maior que a data inicial.");
        }
        if (!primeiroPagamento.isAfter(dataInicial) || !primeiroPagamento.isBefore(dataFinal)) {
            throw new IllegalArgumentException(
                    "A data de primeiro pagamento deve ser maior que a data inicial e menor que a data final.");
        }
        if (dataFinal.isAfter(dataInicial.plusYears(PERIODO_MAXIMO_ANOS))) {
            throw new IllegalArgumentException(
                    "O período entre a data inicial e a data final não pode exceder " + PERIODO_MAXIMO_ANOS + " anos.");
        }
        if (valorEmprestimo.signum() <= 0) {
            throw new IllegalArgumentException("O valor do empréstimo deve ser maior que zero.");
        }
        if (taxaJuros.signum() < 0) {
            throw new IllegalArgumentException("A taxa de juros não pode ser negativa.");
        }
    }

    /** Limite de segurança que evita cálculos de tamanho ilimitado. */
    public static final int PERIODO_MAXIMO_ANOS = 100;
}
