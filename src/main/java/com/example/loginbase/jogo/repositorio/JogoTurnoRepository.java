package com.example.loginbase.jogo.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.loginbase.jogo.modelo.JogoTurno;

public interface JogoTurnoRepository extends JpaRepository<JogoTurno, Integer> {

	Optional<JogoTurno> findByNumero(Integer numero);

	boolean existsByNumero(Integer numero);

	@Query("select coalesce(max(t.numero), 0) from JogoTurno t")
	int maiorNumero();

	@Query(value = "select pg_try_advisory_xact_lock(:chave)", nativeQuery = true)
	boolean tentarTravaDoTurno(@Param("chave") long chave);

}
