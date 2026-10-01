package com.example.loginbase.jogo.comercio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdemComercioRepository extends JpaRepository<OrdemComercio, Long> {

	@Query("select coalesce(sum(o.quantidade), 0) from OrdemComercio o where o.vilaId = :vilaId and o.turno = :turno")
	long volumeDoTurno(@Param("vilaId") Long vilaId, @Param("turno") int turno);

	List<OrdemComercio> findByVilaIdOrderByIdDesc(Long vilaId);

}
