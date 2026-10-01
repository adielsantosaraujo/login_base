package com.example.loginbase.jogo.servico;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.dto.PreviaVilaDTO;
import com.example.loginbase.jogo.dto.VilaResumoDTO;
import com.example.loginbase.jogo.excecao.RegiaoNaoAdjacenteException;
import com.example.loginbase.jogo.excecao.UrbanaObrigatoriaException;
import com.example.loginbase.jogo.excecao.VilaJaExisteException;
import com.example.loginbase.jogo.excecao.VilaNaoEncontradaException;
import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.Jazida;
import com.example.loginbase.jogo.modelo.LadrilhoJazida;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;
import com.example.loginbase.jogo.repositorio.LadrilhoJazidaRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

/**
 * Criação e consulta da vila do usuário, e prévia das jazidas por semente.
 */
@Service
public class VilaService {

	private static final int REGIOES_INICIAIS = 3;
	private static final int[] X_CASAS_INICIAIS = { 0, 2, 4, 6 };

	private static final Map<Recurso, BigDecimal> ESTOQUE_INICIAL = new EnumMap<>(Recurso.class);

	static {
		ESTOQUE_INICIAL.put(Recurso.MADEIRA, BigDecimal.valueOf(200));
		ESTOQUE_INICIAL.put(Recurso.PEDRA, BigDecimal.valueOf(100));
		ESTOQUE_INICIAL.put(Recurso.ARGILA, BigDecimal.valueOf(50));
		ESTOQUE_INICIAL.put(Recurso.TABUA, BigDecimal.valueOf(20));
		ESTOQUE_INICIAL.put(Recurso.GRAOS, BigDecimal.valueOf(200));
		ESTOQUE_INICIAL.put(Recurso.CARNE, BigDecimal.valueOf(40));
		ESTOQUE_INICIAL.put(Recurso.OURO, BigDecimal.valueOf(200));
	}

	private final VilaRepository vilaRepository;
	private final RegiaoRepository regiaoRepository;
	private final LadrilhoJazidaRepository ladrilhoRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final JogoTurnoRepository turnoRepository;
	private final UsuarioRepository usuarioRepository;
	private final GeradorJazidaService gerador;
	private final EstoqueService estoqueService;

	public VilaService(VilaRepository vilaRepository, RegiaoRepository regiaoRepository,
			LadrilhoJazidaRepository ladrilhoRepository, ConstrucaoRepository construcaoRepository,
			JogoTurnoRepository turnoRepository, UsuarioRepository usuarioRepository,
			GeradorJazidaService gerador, EstoqueService estoqueService) {
		this.vilaRepository = vilaRepository;
		this.regiaoRepository = regiaoRepository;
		this.ladrilhoRepository = ladrilhoRepository;
		this.construcaoRepository = construcaoRepository;
		this.turnoRepository = turnoRepository;
		this.usuarioRepository = usuarioRepository;
		this.gerador = gerador;
		this.estoqueService = estoqueService;
	}

	@Transactional
	public Vila criarVila(Long usuarioId, List<Integer> indices, Map<Integer, TipoRegiao> tipos, Long semente) {
		if (vilaRepository.existsByUsuarioId(usuarioId)) {
			throw new VilaJaExisteException("Usuário já tem vila");
		}
		validarEscolha(indices, tipos);

		long sementeFinal = semente != null ? semente : novaSemente();
		int maiorTurno = turnoRepository.maiorNumero();
		int turnoCriacao = maiorTurno > 0 ? maiorTurno : 1;
		String nome = usuarioRepository.findById(usuarioId).map(u -> "Vila de " + u.getNome())
				.orElse("Vila");

		Vila vila = vilaRepository.save(new Vila(usuarioId, nome, sementeFinal, turnoCriacao));

		for (int i = 1; i <= GradeRegioes.TOTAL; i++) {
			Regiao regiao = new Regiao(vila.getId(), i);
			if (indices.contains(i)) {
				regiao.setPossuida(true);
				regiao.setTipo(tipos.get(i));
			}
			regiao = regiaoRepository.save(regiao);
			if (regiao.isPossuida()) {
				List<LadrilhoJazida> ladrilhos = gerador.gerarLadrilhos(sementeFinal, i, regiao.getId());
				ladrilhoRepository.saveAll(ladrilhos);
			}
		}

		estoqueService.inicializar(vila, ESTOQUE_INICIAL);

		int regiaoUrbana = indices.stream().filter(i -> tipos.get(i) == TipoRegiao.URBANA).findFirst()
				.orElseThrow();
		for (int x : X_CASAS_INICIAIS) {
			construcaoRepository.save(novaCasa(vila.getId(), regiaoUrbana, x, 0));
		}
		return vila;
	}

	@Transactional(readOnly = true)
	public VilaResumoDTO resumo(Long usuarioId) {
		Vila vila = vilaRepository.findByUsuarioId(usuarioId)
				.orElseThrow(() -> new VilaNaoEncontradaException("Vila não encontrada"));
		return resumo(vila);
	}

	@Transactional(readOnly = true)
	public VilaResumoDTO resumo(Vila vila) {
		List<VilaResumoDTO.Regiao> regioes = regiaoRepository.findAllByVilaId(vila.getId()).stream()
				.sorted(Comparator.comparing(Regiao::getIndice))
				.map(r -> new VilaResumoDTO.Regiao(r.getIndice(), r.getTipo(), r.isPossuida()))
				.toList();
		Map<String, BigDecimal> estoque = new LinkedHashMap<>();
		estoqueService.listar(vila).forEach((r, q) -> estoque.put(r.name(), q));
		return new VilaResumoDTO(vila.getId(), vila.getNome(), vila.getSemente(), vila.getTurnoCriacao(),
				regioes, estoque);
	}

	/** Contagem de jazidas por tipo em cada uma das 16 regiões para a semente (gerada se ausente). */
	public PreviaVilaDTO previa(Long semente) {
		long s = semente != null ? semente : novaSemente();
		List<PreviaVilaDTO.Regiao> regioes = new ArrayList<>(GradeRegioes.TOTAL);
		for (int i = 1; i <= GradeRegioes.TOTAL; i++) {
			Map<String, Integer> contagem = new LinkedHashMap<>();
			for (Jazida j : Jazida.values()) {
				contagem.put(j.name(), 0);
			}
			gerador.gerarJazidasPorRegiao(s, i).values().forEach(j -> contagem.merge(j.name(), 1, Integer::sum));
			regioes.add(new PreviaVilaDTO.Regiao(i, contagem));
		}
		return new PreviaVilaDTO(s, regioes);
	}

	private void validarEscolha(List<Integer> indices, Map<Integer, TipoRegiao> tipos) {
		if (indices == null || indices.size() != REGIOES_INICIAIS) {
			throw requisicaoInvalida("Escolha exatamente 3 regiões");
		}
		Set<Integer> unicos = new HashSet<>(indices);
		if (unicos.size() != REGIOES_INICIAIS) {
			throw requisicaoInvalida("As regiões escolhidas devem ser distintas");
		}
		for (Integer i : indices) {
			if (i == null || i < 1 || i > GradeRegioes.TOTAL) {
				throw requisicaoInvalida("Índice de região inválido: " + i);
			}
		}
		if (tipos == null) {
			throw requisicaoInvalida("Informe o tipo de cada região escolhida");
		}
		for (Integer i : indices) {
			if (tipos.get(i) == null) {
				throw requisicaoInvalida("Tipo não informado para a região " + i);
			}
		}
		if (!GradeRegioes.adjacente(indices.get(0), indices.get(1))) {
			throw new RegiaoNaoAdjacenteException("A segunda região não é adjacente à primeira");
		}
		if (!GradeRegioes.adjacente(indices.get(2), indices.get(0))
				&& !GradeRegioes.adjacente(indices.get(2), indices.get(1))) {
			throw new RegiaoNaoAdjacenteException("A terceira região não é adjacente à primeira nem à segunda");
		}
		if (indices.stream().noneMatch(i -> tipos.get(i) == TipoRegiao.URBANA)) {
			throw new UrbanaObrigatoriaException("Ao menos 1 região Urbana é obrigatória");
		}
	}

	private static JogoException requisicaoInvalida(String mensagem) {
		return new JogoException(HttpStatus.BAD_REQUEST, mensagem);
	}

	private static long novaSemente() {
		return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
	}

	private static Construcao novaCasa(Long vilaId, int regiaoIndice, int x, int y) {
		Construcao c = new Construcao();
		c.setVilaId(vilaId);
		c.setTipo(TipoConstrucao.CASA);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(regiaoIndice);
		c.setX(x);
		c.setY(y);
		c.setTamanho(1);
		c.setEstado(EstadoConstrucao.ATIVA);
		c.setPoTotal(0);
		c.setPoAtual(0);
		return c;
	}

}
