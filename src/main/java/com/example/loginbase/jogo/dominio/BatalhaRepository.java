package com.example.loginbase.jogo.dominio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatalhaRepository extends JpaRepository<Batalha, Long> {

	List<Batalha> findByVilaId(Long vilaId);

	/**
	 * Batalha {@code EM_ANDAMENTO} da vila, se houver (no máximo uma, pelo
	 * índice único parcial de {@code jogo_batalhas}) — usada por
	 * {@code MasmorraService.iniciar} para rejeitar uma segunda batalha
	 * concorrente com {@code BATALHA_EM_ANDAMENTO}.
	 */
	Optional<Batalha> findByVilaIdAndStatus(Long vilaId, StatusBatalha status);

}
