package com.vega.armazem.repository;

import com.vega.armazem.domain.Compartimento;
import com.vega.armazem.domain.Ingrediente;
import com.vega.armazem.domain.TipoIngrediente;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    @EntityGraph(attributePaths = "compartimento")
    List<Ingrediente> findAll(Sort sort);

    @EntityGraph(attributePaths = "compartimento")
    Optional<Ingrediente> findByUuid(UUID uuid);

    Optional<Ingrediente> findByCompartimentoAndNomeIgnoreCase(Compartimento compartimento, String nome);

    /** Usado antes de travar o compartimento, sem carregar o ingrediente na sessao. */
    @Query("select i.compartimento.id from Ingrediente i where i.uuid = :uuid")
    Optional<Long> findCompartimentoIdByUuid(@Param("uuid") UUID uuid);

    @Query("select i.tipo as tipo, sum(i.quantidade) as total from Ingrediente i group by i.tipo")
    List<TotalPorTipo> somarQuantidadePorTipo();

    interface TotalPorTipo {
        TipoIngrediente getTipo();

        BigDecimal getTotal();
    }
}
