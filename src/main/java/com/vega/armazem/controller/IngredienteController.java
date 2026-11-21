package com.vega.armazem.controller;

import com.vega.armazem.dto.EntradaRequest;
import com.vega.armazem.dto.EntradaResultado;
import com.vega.armazem.dto.IngredienteResponse;
import com.vega.armazem.dto.SaidaRequest;
import com.vega.armazem.dto.VolumeResponse;
import com.vega.armazem.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ingredientes")
@Tag(name = "Ingredientes")
public class IngredienteController {

    private final EstoqueService estoqueService;

    public IngredienteController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @PostMapping
    @Operation(summary = "Entrada de ingrediente",
            description = "201 quando cria um ingrediente novo; 200 quando soma no mesmo nome do mesmo compartimento.")
    public ResponseEntity<IngredienteResponse> registrarEntrada(@Valid @RequestBody EntradaRequest request) {
        EntradaResultado resultado = estoqueService.registrarEntrada(request);
        if (!resultado.criado()) {
            return ResponseEntity.ok(resultado.ingrediente());
        }
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resultado.ingrediente().id())
                .toUri();
        return ResponseEntity.created(local).body(resultado.ingrediente());
    }

    @PostMapping("/{id}/saida")
    @Operation(summary = "Saída (retirada/venda) de ingrediente")
    public IngredienteResponse registrarSaida(@PathVariable UUID id, @Valid @RequestBody SaidaRequest request) {
        return estoqueService.registrarSaida(id, request);
    }

    @GetMapping
    @Operation(summary = "Lista os ingredientes")
    public List<IngredienteResponse> listar() {
        return estoqueService.listarIngredientes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um ingrediente pelo id (UUID)")
    public IngredienteResponse buscar(@PathVariable UUID id) {
        return estoqueService.buscarIngrediente(id);
    }

    @GetMapping("/volume")
    @Operation(summary = "Total armazenado por tipo")
    public List<VolumeResponse> volume() {
        return estoqueService.volumePorTipo();
    }
}
