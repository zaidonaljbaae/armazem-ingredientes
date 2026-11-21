package com.vega.armazem.controller;

import com.vega.armazem.domain.TipoIngrediente;
import com.vega.armazem.dto.CompartimentoResponse;
import com.vega.armazem.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/compartimentos")
@Tag(name = "Compartimentos")
public class CompartimentoController {

    private final EstoqueService estoqueService;

    public CompartimentoController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    @Operation(summary = "Lista os 5 compartimentos")
    public List<CompartimentoResponse> listar() {
        return estoqueService.listarCompartimentos();
    }

    @GetMapping("/disponiveis")
    @Operation(summary = "Compartimentos com espaço livre para a quantidade e que aceitam o tipo hoje")
    public List<CompartimentoResponse> disponiveis(@RequestParam BigDecimal quantidade,
                                                   @RequestParam TipoIngrediente tipo) {
        return estoqueService.compartimentosDisponiveis(quantidade, tipo);
    }

    @GetMapping("/disponiveis-para-venda")
    @Operation(summary = "Compartimentos do tipo com estoque maior que zero")
    public List<CompartimentoResponse> disponiveisParaVenda(@RequestParam TipoIngrediente tipo) {
        return estoqueService.compartimentosParaVenda(tipo);
    }
}
