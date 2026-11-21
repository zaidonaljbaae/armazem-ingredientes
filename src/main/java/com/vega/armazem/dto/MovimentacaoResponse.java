package com.vega.armazem.dto;

import com.vega.armazem.domain.Movimentacao;
import com.vega.armazem.domain.Operacao;
import com.vega.armazem.domain.TipoIngrediente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MovimentacaoResponse(
        UUID id,
        LocalDateTime dataHora,
        Operacao operacao,
        BigDecimal quantidade,
        TipoIngrediente tipo,
        String responsavel,
        String compartimento,
        UUID ingredienteId,
        String ingrediente
) {

    public static MovimentacaoResponse de(Movimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getUuid(),
                movimentacao.getDataHora(),
                movimentacao.getOperacao(),
                movimentacao.getQuantidade(),
                movimentacao.getTipo(),
                movimentacao.getResponsavel(),
                movimentacao.getCompartimento().getCodigo(),
                movimentacao.getIngrediente().getUuid(),
                movimentacao.getIngrediente().getNome());
    }
}
