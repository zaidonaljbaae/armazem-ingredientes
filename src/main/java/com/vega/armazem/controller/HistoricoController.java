package com.vega.armazem.controller;

import com.vega.armazem.dto.MovimentacaoResponse;
import com.vega.armazem.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/historico")
@Tag(name = "Histórico")
public class HistoricoController {

    private final EstoqueService estoqueService;

    public HistoricoController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    @Operation(summary = "Movimentações ordenadas (sortBy = date | compartimento, order = asc | desc)")
    public List<MovimentacaoResponse> listar(@RequestParam(defaultValue = "date") String sortBy,
                                             @RequestParam(defaultValue = "desc") String order) {
        return estoqueService.historico(sortBy, order);
    }
}
