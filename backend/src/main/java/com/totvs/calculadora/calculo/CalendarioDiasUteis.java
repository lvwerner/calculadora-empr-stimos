package com.totvs.calculadora.calculo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calendário de dias úteis com os feriados nacionais (calendário bancário).
 * <p>
 * Os feriados são calculados apenas para os anos do período informado, evitando
 * cache global sem limite de crescimento.
 */
public final class CalendarioDiasUteis {

    private static final List<MonthDay> FERIADOS_FIXOS = List.of(
            MonthDay.of(1, 1),   // Confraternização Universal
            MonthDay.of(4, 21),  // Tiradentes
            MonthDay.of(5, 1),   // Dia do Trabalho
            MonthDay.of(9, 7),   // Independência
            MonthDay.of(10, 12), // Nossa Senhora Aparecida
            MonthDay.of(11, 2),  // Finados
            MonthDay.of(11, 15), // Proclamação da República
            MonthDay.of(12, 25)  // Natal
    );

    private static final MonthDay CONSCIENCIA_NEGRA = MonthDay.of(11, 20);
    private static final int ANO_INICIO_CONSCIENCIA_NEGRA = 2024; // Lei 14.759/2023

    private final Set<LocalDate> feriados;

    private CalendarioDiasUteis(Set<LocalDate> feriados) {
        this.feriados = feriados;
    }

    /**
     * Cria o calendário cobrindo os anos entre {@code inicio} e {@code fim}, com um
     * ano a mais para permitir avançar datas do fim do período.
     */
    public static CalendarioDiasUteis paraPeriodo(LocalDate inicio, LocalDate fim) {
        Set<LocalDate> feriados = new HashSet<>();
        for (int ano = inicio.getYear(); ano <= fim.getYear() + 1; ano++) {
            adicionarFeriados(ano, feriados);
        }
        return new CalendarioDiasUteis(feriados);
    }

    public boolean isDiaUtil(LocalDate data) {
        DayOfWeek dia = data.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY && !feriados.contains(data);
    }

    /** Retorna a própria data, se for dia útil, ou o próximo dia útil. */
    public LocalDate proximoDiaUtil(LocalDate data) {
        LocalDate atual = data;
        while (!isDiaUtil(atual)) {
            atual = atual.plusDays(1);
        }
        return atual;
    }

    private static void adicionarFeriados(int ano, Set<LocalDate> feriados) {
        for (MonthDay feriado : FERIADOS_FIXOS) {
            feriados.add(feriado.atYear(ano));
        }
        if (ano >= ANO_INICIO_CONSCIENCIA_NEGRA) {
            feriados.add(CONSCIENCIA_NEGRA.atYear(ano));
        }
        LocalDate pascoa = domingoDePascoa(ano);
        feriados.add(pascoa.minusDays(48)); // Segunda-feira de Carnaval
        feriados.add(pascoa.minusDays(47)); // Terça-feira de Carnaval
        feriados.add(pascoa.minusDays(2));  // Sexta-feira Santa
        feriados.add(pascoa.plusDays(60));  // Corpus Christi
    }

    /** Algoritmo de Meeus/Jones/Butcher para o calendário gregoriano. */
    static LocalDate domingoDePascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ano, mes, dia);
    }
}
