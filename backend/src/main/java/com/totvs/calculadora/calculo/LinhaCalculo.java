package com.totvs.calculadora.calculo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Uma linha da grid, equivalente a uma linha da planilha "Calculadora_Emprestimos.xlsx".
 *
 * @param consolidada número da parcela no formato "k/n", ou {@code null} se a data não é de pagamento
 */
public record LinhaCalculo(
        LocalDate dataCompetencia,
        BigDecimal valorEmprestimo,
        BigDecimal saldoDevedor,
        String consolidada,
        BigDecimal total,
        BigDecimal amortizacao,
        BigDecimal saldo,
        BigDecimal provisao,
        BigDecimal acumulado,
        BigDecimal pago) {
}
