package com.vega.armazem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao",
        indexes = @Index(name = "idx_movimentacao_data_hora", columnList = "data_hora, compartimento_id"))
public class Movimentacao extends BaseEntity {

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Operacao operacao;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoIngrediente tipo;

    @Column(nullable = false, length = 100)
    private String responsavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compartimento_id", nullable = false)
    private Compartimento compartimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    protected Movimentacao() {
    }

    public Movimentacao(LocalDateTime dataHora, Operacao operacao, BigDecimal quantidade,
                        String responsavel, Ingrediente ingrediente) {
        this.dataHora = dataHora;
        this.operacao = operacao;
        this.quantidade = quantidade;
        this.tipo = ingrediente.getTipo();
        this.responsavel = responsavel;
        this.compartimento = ingrediente.getCompartimento();
        this.ingrediente = ingrediente;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Operacao getOperacao() {
        return operacao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public TipoIngrediente getTipo() {
        return tipo;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public Compartimento getCompartimento() {
        return compartimento;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }
}
