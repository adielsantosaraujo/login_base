package com.example.loginbase.jogo.servico;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.dto.RegiaoTerrenoDTO;
import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.RegiaoTerreno;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.repositorio.LadrilhoRepository;
import com.example.loginbase.jogo.repositorio.RegiaoTerrenoRepository;
import com.example.loginbase.jogo.servico.GeradorLadrilhoService.PercentualTerreno;

/**
 * Terrenos (percentuais) das regiões e geração/consulta dos ladrilhos de cada região.
 */
@Service
public class TerrenoRegiaoService {

	private final RegiaoTerrenoRepository regiaoTerrenoRepository;
	private final LadrilhoRepository ladrilhoRepository;
	private final GeradorLadrilhoService geradorLadrilho;

	public TerrenoRegiaoService(RegiaoTerrenoRepository regiaoTerrenoRepository,
			LadrilhoRepository ladrilhoRepository, GeradorLadrilhoService geradorLadrilho) {
		this.regiaoTerrenoRepository = regiaoTerrenoRepository;
		this.ladrilhoRepository = ladrilhoRepository;
		this.geradorLadrilho = geradorLadrilho;
	}

	/** Terrenos de cada região (ordenados por posição), indexados pelo id da região. */
	@Transactional(readOnly = true)
	public Map<Long, List<RegiaoTerrenoDTO>> terrenosDasRegioes(Collection<Long> regiaoIds) {
		if (regiaoIds == null || regiaoIds.isEmpty()) {
			return Map.of();
		}
		return regiaoTerrenoRepository.findByRegiaoIdIn(regiaoIds).stream()
				.sorted(Comparator.comparingInt(RegiaoTerreno::getPosicao))
				.collect(Collectors.groupingBy(RegiaoTerreno::getRegiaoId,
						Collectors.mapping(TerrenoRegiaoService::paraDto, Collectors.toList())));
	}

	@Transactional(readOnly = true)
	public List<RegiaoTerrenoDTO> terrenosDaRegiao(long regiaoId) {
		return regiaoTerrenoRepository.findByRegiaoIdOrderByPosicao(regiaoId).stream()
				.map(TerrenoRegiaoService::paraDto).toList();
	}

	/** Gera e grava os 100 ladrilhos da região, se ainda não existirem. */
	@Transactional
	public void gerarLadrilhosSeAusentes(long semente, Regiao regiao) {
		if (ladrilhoRepository.existsByRegiaoId(regiao.getId())) {
			return;
		}
		List<PercentualTerreno> percentuais = regiaoTerrenoRepository.findByRegiaoIdOrderByPosicao(regiao.getId())
				.stream().map(t -> new PercentualTerreno(t.getTerreno(), t.getPercentual())).toList();
		long regiaoId = regiao.getId();
		ladrilhoRepository.saveAll(geradorLadrilho.gerar(semente, regiao.getIndice(), percentuais).stream()
				.map(g -> new Ladrilho(regiaoId, g.x(), g.y(), g.terreno(), g.bonusBase(), g.bonusAdjacente()))
				.toList());
	}

	/** Os primeiros ladrilhos do terreno na região, em ordem de varredura (y, depois x). */
	@Transactional(readOnly = true)
	public List<Ladrilho> primeirosLadrilhos(long regiaoId, TipoTerreno terreno, int quantidade) {
		return ladrilhoRepository.findByRegiaoIdAndTerrenoOrderByYAscXAsc(regiaoId, terreno).stream()
				.limit(quantidade).toList();
	}

	private static RegiaoTerrenoDTO paraDto(RegiaoTerreno t) {
		return new RegiaoTerrenoDTO(t.getTerreno(), t.getPosicao(), t.getPercentual());
	}

}
