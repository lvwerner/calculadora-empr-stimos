package com.totvs.calculadora.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.totvs.calculadora.calculo.CalculadoraEmprestimo;

@WebMvcTest(CalculoEmprestimoController.class)
@Import(CalculadoraEmprestimo.class)
class CalculoEmprestimoControllerTest {

    private static final String URL = "/api/emprestimos/calculos";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCalcularOExemploDaPlanilha() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"dataInicial":"2024-01-01","dataFinal":"2034-01-01","primeiroPagamento":"2024-02-15",
                         "valorEmprestimo":140000,"taxaJuros":7}
                        """))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(jsonPath("$", hasSize(241)))
                .andExpect(jsonPath("$[0].dataCompetencia").value("2024-01-01"))
                .andExpect(jsonPath("$[1].saldoDevedor").value(140791.58))
                .andExpect(jsonPath("$[2].consolidada").value("1/120"))
                .andExpect(jsonPath("$[2].total").value(2355.71))
                .andExpect(jsonPath("$[240].consolidada").value("120/120"));
    }

    @Test
    void deveRetornarErroQuandoCamposObrigatoriosAusentes() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros", hasSize(5)));
    }

    @Test
    void deveValidarAsRegrasDeDatas() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"dataInicial":"2024-01-01","dataFinal":"2023-12-01","primeiroPagamento":"2024-02-15",
                         "valorEmprestimo":1000,"taxaJuros":7}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].mensagem", hasItem("A data final deve ser maior que a data inicial.")))
                .andExpect(jsonPath("$.erros[*].mensagem", hasItem(
                        "A data de primeiro pagamento deve ser maior que a data inicial e menor que a data final.")));
    }

    @Test
    void deveRejeitarPeriodoMuitoLongoEValoresInvalidos() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"dataInicial":"2024-01-01","dataFinal":"2200-01-01","primeiroPagamento":"2024-02-15",
                         "valorEmprestimo":-5,"taxaJuros":-1}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros", hasSize(3)));
    }

    @Test
    void deveRejeitarJsonMalFormado() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"dataInicial\":\"abc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarTipoDeConteudoNaoSuportado() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
