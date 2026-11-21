package com.vega.armazem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "ingrediente",
        uniqueConstraints = @UniqueConstraint(name = "uk_ingrediente_nome_compartimento",
                columnNames = {"nome", "compartimento_id"}))
public class Ingrediente extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoIngrediente tipo;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidade = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compartimento_id", nullable = false)
    private Compartimento compartimento;

    protected Ingrediente() {
    }

    public Ingrediente(String nome, TipoIngrediente tipo, Compartimento compartimento) {
        this.nome = nome;
        this.tipo = tipo;
        this.compartimento = compartimento;
    }

    public String getNome() {
        return nome;
    }

    public TipoIngrediente getTipo() {
        return tipo;
    }

    public void setTipo(TipoIngrediente tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public Compartimento getCompartimento() {
        return compartimento;
    }
}
