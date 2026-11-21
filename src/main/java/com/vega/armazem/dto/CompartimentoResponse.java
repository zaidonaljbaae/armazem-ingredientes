package com.vega.armazem.dto;

import com.vega.armazem.domain.TipoIngrediente;

import java.math.BigDecimal;
import java.util.UUID;

/** {@code capacidade} e {@code espacoLivre} ficam nulos enquanto o compartimento nunca recebeu um tipo. */
public record CompartimentoResponse(
        UUID id,
        String codigo,
        TipoIngrediente tipoAtual,
        BigDecimal capacidade,
        BigDecimal quantidadeAtual,
        BigDecimal espacoLivre
) {
}
