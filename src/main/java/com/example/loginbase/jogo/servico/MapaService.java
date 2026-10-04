package com.example.loginbase.jogo.servico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.masmorra.Masmorra;
import com.example.loginbase.jogo.masmorra.MasmorraRepository;
import com.example.loginbase.jogo.dto.LadrilhoDTO;
import com.example.loginbase.jogo.dto.LadrilhoDTO.ConstrucaoLadrilhoDTO;
import com.example.loginbase.jogo.dto.MapaDTO;
import com.example.loginbase.jogo.dto.MapaDTO.VilaResumoDTO;
import com.example.loginbase.jogo.dto.RegiaoDetalheDTO;
import com.example.loginbase.jogo.dto.RegiaoDetalheDTO.RegiaoDTO;
import com.example.loginbase.jogo.dto.RegiaoResumoDTO;
import com.example.loginbase.jogo.dto.RegiaoTerrenoDTO;
import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.repositorio.LadrilhoRepository;
import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;

/**
 * Leitura do mapa da vila (grade 4x4) e do detalhe de uma região (10x10 ladrilhos).
 */
@Service
@Transactional(readOnly = true)
public class MapaService {

	private final RegiaoRepository regiaoRepository;
	private final LadrilhoRepository ladrilhoRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final TerrenoRegiaoService terrenoRegiaoService;
	private final ConsultaMasmorras consultaMasmorras;
	private final MasmorraRepository masmorraRepository;

	public MapaService(RegiaoRepository regiaoRepository, LadrilhoRepository ladrilhoRepository,
			ConstrucaoRepository construcaoRepository, ConsultaMasmorras consultaMasmorras,
			MasmorraRepository masmorraRepository, TerrenoRegiaoService terrenoRegiaoService) {
		this.regiaoRepository = regiaoRepository;
		this.ladrilhoRepository = ladrilhoRepository;
		this.construcaoRepository = construcaoRepository;
		this.consultaMasmorras = consultaMasmorras;
		this.masmorraRepository = masmorraRepository;
		this.terrenoRegiaoService = terrenoRegiaoService;
	}

	public MapaDTO obterMapaVila(Vila vila) {
		Map<Integer, Regiao> porIndice = new HashMap<>();
		regiaoRepository.findAllByVilaId(vila.getId()).forEach(r -> porIndice.put(r.getIndice(), r));
		Map<Integer, Long> masmorraIds = new HashMap<>();
		for (Masmorra m : masmorraRepository.findByVilaIdAndAtivaTrueOrderByIdAsc(vila.getId())) {
			masmorraIds.put(m.getRegiaoIndice(), m.getId());
		}
		Map<Long, List<RegiaoTerrenoDTO>> terrenosPorRegiao = terrenoRegiaoService
				.terrenosDasRegioes(porIndice.values().stream().map(Regiao::getId).toList());
		List<RegiaoResumoDTO> regioes = new ArrayList<>(GradeRegioes.TOTAL);
		for (int i = 1; i <= GradeRegioes.TOTAL; i++) {
			Regiao r = porIndice.get(i);
			boolean possuida = r != null && r.isPossuida();
			TipoRegiao tipo = r != null ? r.getTipo() : null;
			List<RegiaoTerrenoDTO> terrenos = r != null ? terrenosPorRegiao.getOrDefault(r.getId(), List.of()) : List.of();
			Optional<Integer> nivel = !possuida ? consultaMasmorras.nivelMasmorraAtiva(vila, i) : Optional.empty();
			regioes.add(new RegiaoResumoDTO(i, tipo, possuida, nivel.isPresent(), nivel.orElse(null),
					nivel.isPresent() ? masmorraIds.get(i) : null, terrenos));
		}
		return new MapaDTO(new VilaResumoDTO(vila.getId(), vila.getNome()), regioes);
	}

	/** Região não possuída devolve {@code possuida=false} e lista de ladrilhos vazia. */
	public RegiaoDetalheDTO obterRegiaoDetalhada(Vila vila, int indice) {
		if (indice < 1 || indice > GradeRegioes.TOTAL) {
			throw new JogoException(HttpStatus.NOT_FOUND, "Região inexistente: " + indice);
		}
		Optional<Regiao> encontrada = regiaoRepository.findByVilaIdAndIndice(vila.getId(), indice);
		if (encontrada.isEmpty() || !encontrada.get().isPossuida()) {
			Long id = encontrada.map(Regiao::getId).orElse(null);
			TipoRegiao tipoNaoPossuida = encontrada.map(Regiao::getTipo).orElse(null);
			List<RegiaoTerrenoDTO> terrenosNaoPossuida = id == null ? List.of()
					: terrenoRegiaoService.terrenosDaRegiao(id);
			return new RegiaoDetalheDTO(new RegiaoDTO(id, indice, tipoNaoPossuida, false, terrenosNaoPossuida),
					List.of());
		}
		Regiao regiao = encontrada.get();

		Map<String, ConstrucaoLadrilhoDTO> construcoes = new HashMap<>();
		for (Construcao c : construcaoRepository.findByVilaIdAndRegiaoIndice(vila.getId(), indice)) {
			int tam = c.getTamanho() == null ? 1 : Math.max(c.getTamanho(), 1);
			ConstrucaoLadrilhoDTO dto = new ConstrucaoLadrilhoDTO(c.getId(), c.getTipo(), c.getNivel(), tam,
					c.getEstado(), c.getPoAtual() == null ? 0 : c.getPoAtual(),
					c.getPoTotal() == null ? 0 : c.getPoTotal());
			for (int dx = 0; dx < tam; dx++) {
				for (int dy = 0; dy < tam; dy++) {
					construcoes.put(chave(c.getX() + dx, c.getY() + dy), dto);
				}
			}
		}

		List<LadrilhoDTO> ladrilhos = new ArrayList<>(GeradorLadrilhoService.TOTAL_LADRILHOS);
		for (Ladrilho l : ladrilhoRepository.findByRegiaoIdOrderByYAscXAsc(regiao.getId())) {
			ladrilhos.add(new LadrilhoDTO(l.getX(), l.getY(), l.getTerreno(), l.getBonusBase(), l.getBonusAdjacente(),
					l.getBonusTotal(), construcoes.get(chave(l.getX(), l.getY()))));
		}
		return new RegiaoDetalheDTO(new RegiaoDTO(regiao.getId(), indice, regiao.getTipo(), true,
				terrenoRegiaoService.terrenosDaRegiao(regiao.getId())), ladrilhos);
	}

	private static String chave(int x, int y) {
		return x + "," + y;
	}

}
