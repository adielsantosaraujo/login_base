package com.example.loginbase.jogo.cidadao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CidadaoRepository extends JpaRepository<Cidadao, Long> {

	List<Cidadao> findByVilaId(Long vilaId);

	List<Cidadao> findByVilaIdAndVivoTrue(Long vilaId);

	List<Cidadao> findByFamiliaId(Long familiaId);

	List<Cidadao> findByFamiliaIdAndVivoTrue(Long familiaId);

	List<Cidadao> findByConstrucaoId(Long construcaoId);

	List<Cidadao> findByConstrucaoIdAndVivoTrue(Long construcaoId);

	List<Cidadao> findByTropaId(Long tropaId);

	List<Cidadao> findByVilaIdAndVivoTrueAndEstado(Long vilaId, EstadoCidadao estado);

	long countByTropaId(Long tropaId);

	List<Cidadao> findByConjugeId(Long conjugeId);

	/** Casais vivos da vila: um registro por casal (o cônjuge de sexo M). */
	@Query("select c from Cidadao c where c.vilaId = :vilaId and c.vivo = true and c.conjugeId is not null and c.sexo = com.example.loginbase.jogo.cidadao.Sexo.M")
	List<Cidadao> findCasaisVivos(Long vilaId);

}
