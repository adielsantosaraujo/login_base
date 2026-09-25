package com.example.loginbase.acesso;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

	Optional<Sessao> findByTokenAndDataFimIsNull(String token);

	/**
	 * Fecha (preenche {@code dataFim}) todas as sessões ainda abertas. É uma
	 * atualização em lote via JPQL, por isso não passa pelo
	 * {@code AuditingEntityListener}; {@code alteradoEm}/{@code alteradoPor}
	 * são preenchidos explicitamente pelos parâmetros.
	 */
	@Modifying(clearAutomatically = true)
	@Query("update Sessao s set s.dataFim = :agora, s.alteradoEm = :agora, s.alteradoPor = :por where s.dataFim is null")
	int fecharTodasAbertas(@Param("agora") Instant agora, @Param("por") String por);

}
