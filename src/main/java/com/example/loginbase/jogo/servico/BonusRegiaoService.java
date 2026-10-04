package com.example.loginbase.jogo.servico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.dto.RegiaoBonusDTO;
import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.RegiaoBonus;
import com.example.loginbase.jogo.repositorio.RegiaoBonusRepository;

/**
 * Bônus de região da vila: soma dos bônus das regiões possuídas e fator de produção.
 */
@Service
public class BonusRegiaoService {

	private final RegiaoBonusRepository regiaoBonusRepository;

	public BonusRegiaoService(RegiaoBonusRepository regiaoBonusRepository) {
		this.regiaoBonusRepository = regiaoBonusRepository;
	}

	/** Soma por bônus das regiões possuídas; sempre com as 13 chaves (0 quando ausente). */
	@Transactional(readOnly = true)
	public Map<BonusRegiao, Integer> bonusDaVila(Long vilaId) {
		Map<BonusRegiao, Integer> mapa = new EnumMap<>(BonusRegiao.class);
		for (BonusRegiao b : BonusRegiao.values()) {
			mapa.put(b, 0);
		}
		for (RegiaoBonusRepository.TotalBonus t : regiaoBonusRepository.somarBonusPossuidos(vilaId)) {
			mapa.put(t.getBonus(), t.getTotal() == null ? 0 : t.getTotal().intValue());
		}
		return mapa;
	}

	/** Bônus (ordenados por posição) de cada região informada, possuída ou não. */
	@Transactional(readOnly = true)
	public Map<Long, List<RegiaoBonusDTO>> bonusDasRegioes(Collection<Long> regiaoIds) {
		Map<Long, List<RegiaoBonusDTO>> mapa = new HashMap<>();
		if (regiaoIds.isEmpty()) {
			return mapa;
		}
		regiaoBonusRepository.findByRegiaoIdIn(regiaoIds).stream()
				.sorted(Comparator.comparingInt(RegiaoBonus::getPosicao))
				.forEach(b -> mapa.computeIfAbsent(b.getRegiaoId(), k -> new ArrayList<>())
						.add(new RegiaoBonusDTO(b.getBonus(), b.getPosicao(), b.getValor())));
		return mapa;
	}

	public int bonus(Long vilaId, BonusRegiao b) {
		return bonusDaVila(vilaId).get(b);
	}

	/** Fator de produção {@code 1 + bônus/100}, escala 2 (ex.: 42 gera 1.42). */
	public BigDecimal fator(Long vilaId, BonusRegiao b) {
		return BigDecimal.ONE.add(BigDecimal.valueOf(bonus(vilaId, b)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
				.setScale(2, RoundingMode.HALF_UP);
	}

}
