package com.example.loginbase.jogo.repositorio;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.RegiaoBonus;

public interface RegiaoBonusRepository extends JpaRepository<RegiaoBonus, Long> {

	List<RegiaoBonus> findByRegiaoIdOrderByPosicao(long regiaoId);

	List<RegiaoBonus> findByRegiaoIdIn(Collection<Long> regiaoIds);

	void deleteByRegiaoIdIn(Collection<Long> regiaoIds);

	/** Soma dos valores de cada bônus nas regiões possuídas da vila. */
	@Query("select b.bonus as bonus, sum(b.valor) as total from RegiaoBonus b, Regiao r "
			+ "where b.regiaoId = r.id and r.vilaId = :vilaId and r.possuida = true group by b.bonus")
	List<TotalBonus> somarBonusPossuidos(@Param("vilaId") Long vilaId);

	interface TotalBonus {
		BonusRegiao getBonus();

		Long getTotal();
	}

}
