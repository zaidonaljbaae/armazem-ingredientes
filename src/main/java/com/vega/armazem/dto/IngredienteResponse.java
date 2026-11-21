package com.vega.armazem.dto;

import com.vega.armazem.domain.Ingrediente;
import com.vega.armazem.domain.TipoIngrediente;

import java.math.BigDecimal;
import java.util.UUID;

public record IngredienteResponse(
        UUID id,
        String nome,
        TipoIngrediente tipo,
        BigDecimal quantidade,
        String compartimento
) {

    public static IngredienteResponse de(Ingrediente ingrediente) {
        return new IngredienteResponse(
                ingrediente.getUuid(),
                ingrediente.getNome(),
                ingrediente.getTipo(),
                ingrediente.getQuantidade(),
                ingrediente.getCompartimento().getCodigo());
    }
}
