package com.totvs.calculadora.calculo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

class CalculadoraEmprestimoTest {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.01");

    private final CalculadoraEmprestimo calculadora = new CalculadoraEmprestimo();

    @Test
    void deveReproduzirTodasAsLinhasDaPlanilhaDeReferencia() throws IOException {
        ParametrosEmprestimo parametros = new ParametrosEmprestimo(
                LocalDate.of(2024, 1, 1), LocalDate.of(2034, 1, 1), LocalDate.of(2024, 2, 15),
                new BigDecimal("140000"), new BigDecimal("7"));

        List<LinhaCalculo> calculadas = calculadora.calcular(parametros);
        List<String[]> esperadas = lerPlanilhaReferencia();

        assertThat(calculadas).hasSize(esperadas.size());
        for (int i = 0; i < esperadas.size(); i++) {
            String[] esperada = esperadas.get(i);
            LinhaCalculo linha = calculadas.get(i);
            String contexto = "linha " + (i + 1) + " (" + esperada[0] + ")";

            assertThat(linha.dataCompetencia()).as(contexto).isEqualTo(LocalDate.parse(esperada[0], DATA_BR));
            assertThat(linha.consolidada()).as(contexto).isEqualTo(esperada[3].isEmpty() ? null : esperada[3]);

            Map<Integer, Function<LinhaCalculo, BigDecimal>> colunas = Map.of(
                    1, LinhaCalculo::valorEmprestimo, 2, LinhaCalculo::saldoDevedor, 4, LinhaCalculo::total,
                    5, LinhaCalculo::amortizacao, 6, LinhaCalculo::saldo, 7, LinhaCalculo::provisao,
                    8, LinhaCalculo::acumulado, 9, LinhaCalculo::pago);
            colunas.forEach((coluna, valor) -> assertThat(valor.apply(linha))
                    .as(contexto + " coluna " + coluna)
                    .isCloseTo(new BigDecimal(esperada[coluna]), within(TOLERANCIA)));
        }
    }

    @Test
    void pagamentoNoDia31DeveCairNoUltimoDiaDosMesesMaisCurtos() {
        ParametrosEmprestimo parametros = new ParametrosEmprestimo(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 30), LocalDate.of(2024, 1, 31),
                new BigDecimal("1000"), new BigDecimal("10"));

        List<LinhaCalculo> linhas = calculadora.calcular(parametros);

        assertThat(linhas).extracting(LinhaCalculo::dataCompetencia, LinhaCalculo::consolidada).containsExactly(
                tuple(LocalDate.of(2024, 1, 1), null),
                tuple(LocalDate.of(2024, 1, 31), "1/4"),
                tuple(LocalDate.of(2024, 2, 29), "2/4"),
                tuple(LocalDate.of(2024, 3, 31), null),
                // 31/03/2024 é domingo: a parcela vai para o próximo dia útil
                tuple(LocalDate.of(2024, 4, 1), "3/4"),
                tuple(LocalDate.of(2024, 4, 30), "4/4"));
    }

    @Test
    void dataFinalSempreEhPagamentoEQuitaOSaldo() {
        ParametrosEmprestimo parametros = new ParametrosEmprestimo(
                LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 20), LocalDate.of(2024, 2, 5),
                new BigDecimal("5000"), new BigDecimal("12"));

        List<LinhaCalculo> linhas = calculadora.calcular(parametros);
        LinhaCalculo ultima = linhas.getLast();

        assertThat(ultima.dataCompetencia()).isEqualTo(LocalDate.of(2024, 3, 20));
        assertThat(ultima.consolidada()).isEqualTo("3/3");
        assertThat(ultima.saldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ultima.saldoDevedor()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ultima.acumulado()).isEqualByComparingTo(BigDecimal.ZERO);
        BigDecimal totalAmortizado = linhas.stream().map(LinhaCalculo::amortizacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalAmortizado).isCloseTo(new BigDecimal("5000"), within(TOLERANCIA));
    }

    @Test
    void naoDeveDuplicarParcelaQuandoCoincideComDataFinal() {
        ParametrosEmprestimo parametros = new ParametrosEmprestimo(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 15), LocalDate.of(2024, 2, 15),
                new BigDecimal("1000"), new BigDecimal("5"));

        List<LinhaCalculo> linhas = calculadora.calcular(parametros);

        assertThat(linhas).extracting(LinhaCalculo::consolidada).filteredOn(Objects::nonNull)
                .containsExactly("1/2", "2/2");
    }

    @Test
    void deveValidarAsDatas() {
        BigDecimal valor = new BigDecimal("1000");
        BigDecimal taxa = new BigDecimal("7");
        LocalDate inicio = LocalDate.of(2024, 1, 1);

        assertThatIllegalArgumentException().isThrownBy(() ->
                new ParametrosEmprestimo(inicio, inicio, inicio.plusDays(1), valor, taxa));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new ParametrosEmprestimo(inicio, inicio.plusMonths(3), inicio, valor, taxa));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new ParametrosEmprestimo(inicio, inicio.plusMonths(3), inicio.plusMonths(3), valor, taxa));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new ParametrosEmprestimo(inicio, inicio.plusYears(101), inicio.plusMonths(1), valor, taxa));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new ParametrosEmprestimo(inicio, inicio.plusMonths(3), inicio.plusMonths(1), BigDecimal.ZERO, taxa));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new ParametrosEmprestimo(inicio, inicio.plusMonths(3), inicio.plusMonths(1), valor, new BigDecimal("-1")));
    }

    private static org.assertj.core.groups.Tuple tuple(Object... valores) {
        return org.assertj.core.groups.Tuple.tuple(valores);
    }

    private static List<String[]> lerPlanilhaReferencia() throws IOException {
        try (var entrada = CalculadoraEmprestimoTest.class.getResourceAsStream("/planilha-referencia.csv");
             var leitor = new BufferedReader(new InputStreamReader(Objects.requireNonNull(entrada), StandardCharsets.UTF_8))) {
            return leitor.lines().skip(1).map(linha -> linha.split(";", -1)).toList();
        }
    }
}
