package com.example.loginbase.jogo.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.loginbase.jogo.modelo.Vila;

public interface VilaRepository extends JpaRepository<Vila, Long> {

	Optional<Vila> findByUsuarioId(Long usuarioId);

	boolean existsByUsuarioId(Long usuarioId);

	@Query("select v.id from Vila v order by v.id")
	List<Long> findAllIds();

	@Query("select v.id from Vila v where v.populacaoConfirmada = true order by v.id")
	List<Long> findIdsComPopulacaoConfirmada();

}
