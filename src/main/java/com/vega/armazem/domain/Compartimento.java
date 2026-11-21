package com.vega.armazem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compartimento")
public class Compartimento extends BaseEntity {

    @Column(nullable = false, unique = true, length = 2)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_atual", length = 20)
    private TipoIngrediente tipoAtual;

    @Column(name = "quantidade_atual", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidadeAtual = BigDecimal.ZERO;

    @Column(name = "data_ultimo_armazenamento")
    private LocalDateTime dataUltimoArmazenamento;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Compartimento() {
    }

    public Compartimento(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoIngrediente getTipoAtual() {
        return tipoAtual;
    }

    public void setTipoAtual(TipoIngrediente tipoAtual) {
        this.tipoAtual = tipoAtual;
    }

    public BigDecimal getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public void setQuantidadeAtual(BigDecimal quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    public LocalDateTime getDataUltimoArmazenamento() {
        return dataUltimoArmazenamento;
    }

    public void setDataUltimoArmazenamento(LocalDateTime dataUltimoArmazenamento) {
        this.dataUltimoArmazenamento = dataUltimoArmazenamento;
    }
}
