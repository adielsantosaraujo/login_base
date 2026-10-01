package com.example.loginbase.jogo.recurso;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

	Optional<Estoque> findByVilaIdAndRecurso(Long vilaId, Recurso recurso);

	List<Estoque> findByVilaId(Long vilaId);

	/** Carrega com bloqueio de escrita, para débitos atômicos. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from Estoque e where e.vilaId = :vilaId and e.recurso in :recursos")
	List<Estoque> findParaAtualizar(@Param("vilaId") Long vilaId, @Param("recursos") Collection<Recurso> recursos);

}
