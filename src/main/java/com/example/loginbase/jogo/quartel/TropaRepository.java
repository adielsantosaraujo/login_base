package com.example.loginbase.jogo.quartel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TropaRepository extends JpaRepository<Tropa, Long> {

	List<Tropa> findByVilaId(Long vilaId);

	List<Tropa> findByQuartelId(Long quartelId);

	Optional<Tropa> findByIdAndVilaId(Long id, Long vilaId);

	List<Tropa> findByVilaIdAndEstadoInOrderByIdAsc(Long vilaId, java.util.Collection<EstadoTropa> estados);

	/** Se o cidadão pertence a uma tropa em viagem (ida ou volta). */
	@Query("select count(c) > 0 from Cidadao c, Tropa t where c.id = :cidadaoId and c.tropaId = t.id "
			+ "and t.estado <> com.example.loginbase.jogo.quartel.EstadoTropa.AQUARTELADA")
	boolean cidadaoEmViagem(Long cidadaoId);

	boolean existsByVilaIdAndNome(Long vilaId, String nome);

}
