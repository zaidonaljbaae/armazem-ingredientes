package com.vega.armazem.dto;

/** {@code criado} = true quando a entrada criou um ingrediente novo (201); false quando somou (200). */
public record EntradaResultado(IngredienteResponse ingrediente, boolean criado) {
}
