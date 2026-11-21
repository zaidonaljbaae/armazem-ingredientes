package com.vega.armazem.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TipoIngredienteTest {

    @Test
    void a02_capacidadesPorTipo() {
        assertThat(TipoIngrediente.SECO.getCapacidade()).isEqualByComparingTo("600");
        assertThat(TipoIngrediente.SECO.getUnidade()).isEqualTo("kg");
        assertThat(TipoIngrediente.LIQUIDO.getCapacidade()).isEqualByComparingTo("500");
        assertThat(TipoIngrediente.LIQUIDO.getUnidade()).isEqualTo("L");
        assertThat(TipoIngrediente.REFRIGERADO.getCapacidade()).isEqualByComparingTo("400");
        assertThat(TipoIngrediente.REFRIGERADO.getUnidade()).isEqualTo("kg");
    }

    @Test
    void a17_aceitaSecoESecos() {
        assertThat(TipoIngrediente.of("SECO")).isEqualTo(TipoIngrediente.SECO);
        assertThat(TipoIngrediente.of("SECOS")).isEqualTo(TipoIngrediente.SECO);
    }
}
