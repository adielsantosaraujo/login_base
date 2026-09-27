package com.example.loginbase.jogo.dominio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface VilaRepository extends JpaRepository<Vila, Long> {

	Optional<Vila> findByUsuarioId(Long usuarioId);

	/**
	 * Busca a vila do usuário com lock pessimista de escrita, para uso no
	 * início de comandos/leituras que disparam
	 * {@code VilaService.sincronizar} (cálculo preguiçoso do tempo) — evita
	 * que duas requisições concorrentes sincronizem/gastem recursos da mesma
	 * vila ao mesmo tempo.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select v from Vila v where v.usuarioId = :usuarioId")
	Optional<Vila> findByUsuarioIdParaAtualizacao(@Param("usuarioId") Long usuarioId);

}
