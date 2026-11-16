package com.totvs.calculadora.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalendarioDiasUteisTest {

    private final CalendarioDiasUteis calendario =
            CalendarioDiasUteis.paraPeriodo(LocalDate.of(2024, 1, 1), LocalDate.of(2034, 1, 1));

    @ParameterizedTest
    @CsvSource({"2024, 2024-03-31", "2026, 2026-04-05", "2028, 2028-04-16", "2033, 2033-04-17"})
    void deveCalcularODomingoDePascoa(int ano, LocalDate esperado) {
        assertThat(CalendarioDiasUteis.domingoDePascoa(ano)).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "2024-02-15, 2024-02-15", // dia útil
            "2024-06-15, 2024-06-17", // sábado
            "2024-09-15, 2024-09-16", // domingo
            "2024-11-15, 2024-11-18", // Proclamação da República (sexta)
            "2026-02-15, 2026-02-18", // domingo + Carnaval
            "2028-06-15, 2028-06-16", // Corpus Christi
            "2033-04-15, 2033-04-18", // Sexta-feira Santa
            "2027-11-15, 2027-11-16", // Proclamação da República (segunda)
            "2025-11-20, 2025-11-21"  // Consciência Negra
    })
    void deveAvancarParaOProximoDiaUtil(LocalDate data, LocalDate esperado) {
        assertThat(calendario.proximoDiaUtil(data)).isEqualTo(esperado);
    }
}
