package com.example.loginbase.jogo.turno;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoTurnoRepository extends JpaRepository<EventoTurno, Long> {

	List<EventoTurno> findByVilaIdAndTurnoOrderByIdAsc(Long vilaId, Integer turno);

	@Query("select max(e.turno) from EventoTurno e where e.vilaId = :vilaId")
	Optional<Integer> maiorTurnoDaVila(@Param("vilaId") Long vilaId);

	long countByVilaIdAndTurno(Long vilaId, Integer turno);

}
