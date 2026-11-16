package com.totvs.calculadora.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.totvs.calculadora.calculo.CalculadoraEmprestimo;
import com.totvs.calculadora.calculo.LinhaCalculo;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/emprestimos", produces = MediaType.APPLICATION_JSON_VALUE)
public class CalculoEmprestimoController {

    private final CalculadoraEmprestimo calculadora;

    public CalculoEmprestimoController(CalculadoraEmprestimo calculadora) {
        this.calculadora = calculadora;
    }

    @PostMapping(path = "/calculos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<LinhaCalculo> calcular(@Valid @RequestBody CalculoEmprestimoRequest request) {
        return calculadora.calcular(request.paraParametros());
    }
}
