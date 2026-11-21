package com.vega.armazem.dto;

import com.vega.armazem.domain.TipoIngrediente;

import java.math.BigDecimal;

/** Nomes em ingles para seguir exatamente o exemplo do PDF. */
public record VolumeResponse(TipoIngrediente type, BigDecimal totalQuantity) {
}
