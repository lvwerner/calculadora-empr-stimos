package com.totvs.calculadora.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.totvs.calculadora.calculo.ParametrosEmprestimo;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/**
 * Dados enviados pela tela.
 *
 * @param taxaJuros taxa em percentual (ex.: 7 para 7%)
 */
public record CalculoEmprestimoRequest(
        @NotNull(message = "A data inicial é obrigatória.")
        LocalDate dataInicial,

        @NotNull(message = "A data final é obrigatória.")
        LocalDate dataFinal,

        @NotNull(message = "A data do primeiro pagamento é obrigatória.")
        LocalDate primeiroPagamento,

        @NotNull(message = "O valor do empréstimo é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor do empréstimo deve ser maior que zero.")
        @Digits(integer = 13, fraction = 2, message = "O valor do empréstimo deve ter no máximo 13 dígitos inteiros e 2 decimais.")
        BigDecimal valorEmprestimo,

        @NotNull(message = "A taxa de juros é obrigatória.")
        @DecimalMin(value = "0", message = "A taxa de juros não pode ser negativa.")
        @DecimalMax(value = "1000", message = "A taxa de juros deve ser no máximo 1000%.")
        @Digits(integer = 4, fraction = 4, message = "A taxa de juros deve ter no máximo 4 casas decimais.")
        BigDecimal taxaJuros) {

    @AssertTrue(message = "A data final deve ser maior que a data inicial.")
    boolean isDataFinal() {
        return dataInicial == null || dataFinal == null || dataFinal.isAfter(dataInicial);
    }

    @AssertTrue(message = "A data de primeiro pagamento deve ser maior que a data inicial e menor que a data final.")
    boolean isPrimeiroPagamento() {
        return primeiroPagamento == null || dataInicial == null || dataFinal == null
                || (primeiroPagamento.isAfter(dataInicial) && primeiroPagamento.isBefore(dataFinal));
    }

    @AssertTrue(message = "O período entre a data inicial e a data final não pode exceder "
            + ParametrosEmprestimo.PERIODO_MAXIMO_ANOS + " anos.")
    boolean isPeriodo() {
        return dataInicial == null || dataFinal == null
                || !dataFinal.isAfter(dataInicial.plusYears(ParametrosEmprestimo.PERIODO_MAXIMO_ANOS));
    }

    ParametrosEmprestimo paraParametros() {
        return new ParametrosEmprestimo(dataInicial, dataFinal, primeiroPagamento, valorEmprestimo, taxaJuros);
    }
}
