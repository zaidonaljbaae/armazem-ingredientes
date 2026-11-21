package com.vega.armazem.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

/**
 * Tipos de ingrediente e a capacidade maxima de um compartimento para cada um.
 * A ordem das constantes e alfabetica, a mesma usada na resposta de volume.
 */
public enum TipoIngrediente {

    LIQUIDO(new BigDecimal("500"), "L"),
    REFRIGERADO(new BigDecimal("400"), "kg"),
    SECO(new BigDecimal("600"), "kg");

    private final BigDecimal capacidade;
    private final String unidade;

    TipoIngrediente(BigDecimal capacidade, String unidade) {
        this.capacidade = capacidade;
        this.unidade = unidade;
    }

    public BigDecimal getCapacidade() {
        return capacidade;
    }

    public String getUnidade() {
        return unidade;
    }

    /**
     * Aceita singular ou plural, maiusculas ou minusculas, com ou sem acento
     * ("SECOS", "seco", "liquido", "LIQUIDO"...).
     */
    @JsonCreator
    public static TipoIngrediente of(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O tipo do ingrediente é obrigatório.");
        }
        String normalizado = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        String singular = normalizado.endsWith("S") ? normalizado.substring(0, normalizado.length() - 1) : normalizado;
        return Arrays.stream(values())
                .filter(tipo -> tipo.name().equals(singular))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de ingrediente inválido: '" + valor + "'. Use SECO, LIQUIDO ou REFRIGERADO."));
    }
}
