package com.vega.armazem.dto;

import com.vega.armazem.domain.TipoIngrediente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Informe {@code compartimentoId} (1..5, como no PDF) ou {@code compartimento} (C1..C5). */
public record EntradaRequest(

        @Schema(example = "Farinha de Trigo")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome,

        @Schema(example = "SECOS", description = "SECO, LIQUIDO ou REFRIGERADO (aceita plural, minusculas e acento)")
        @NotNull(message = "O tipo é obrigatório.")
        TipoIngrediente tipo,

        @Schema(example = "400")
        @NotNull(message = "A quantidade é obrigatória.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        @Digits(integer = 7, fraction = 3, message = "A quantidade pode ter no máximo 3 casas decimais.")
        BigDecimal quantidade,

        @Schema(example = "1")
        Integer compartimentoId,

        @Schema(example = "C1")
        String compartimento,

        @Schema(example = "pedro")
        @NotBlank(message = "O responsável é obrigatório.")
        @Size(max = 100, message = "O responsável pode ter no máximo 100 caracteres.")
        String responsavel
) {
}
