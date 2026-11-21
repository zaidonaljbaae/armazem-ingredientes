package com.vega.armazem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SaidaRequest(

        @Schema(example = "100")
        @NotNull(message = "A quantidade é obrigatória.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        @Digits(integer = 7, fraction = 3, message = "A quantidade pode ter no máximo 3 casas decimais.")
        BigDecimal quantidade,

        @Schema(example = "maria")
        @NotBlank(message = "O responsável é obrigatório.")
        @Size(max = 100, message = "O responsável pode ter no máximo 100 caracteres.")
        String responsavel
) {
}
