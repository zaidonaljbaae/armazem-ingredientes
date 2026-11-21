package com.vega.armazem.repository;

import com.vega.armazem.domain.Movimentacao;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    @EntityGraph(attributePaths = {"compartimento", "ingrediente"})
    List<Movimentacao> findAll(Sort sort);
}
