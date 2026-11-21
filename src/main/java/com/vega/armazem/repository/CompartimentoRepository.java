package com.vega.armazem.repository;

import com.vega.armazem.domain.Compartimento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompartimentoRepository extends JpaRepository<Compartimento, Long> {

    List<Compartimento> findAllByOrderByCodigoAsc();

    boolean existsByCodigo(String codigo);

    /** SELECT ... FOR UPDATE: serializa entradas e saidas no mesmo compartimento. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Compartimento c where c.codigo = :codigo")
    Optional<Compartimento> findByCodigoParaAtualizar(@Param("codigo") String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Compartimento c where c.id = :id")
    Optional<Compartimento> findByIdParaAtualizar(@Param("id") Long id);
}
