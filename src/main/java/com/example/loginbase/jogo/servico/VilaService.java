package com.example.loginbase.jogo.servico;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.cidadao.FamiliaService;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.dto.RegiaoBonusDTO;
import com.example.loginbase.jogo.dto.VilaResumoDTO;
import com.example.loginbase.jogo.excecao.RegiaoNaoAdjacenteException;
import com.example.loginbase.jogo.excecao.SelecaoInvalidaException;
import com.example.loginbase.jogo.excecao.UrbanaObrigatoriaException;
import com.example.loginbase.jogo.excecao.VilaJaExisteException;
import com.example.loginbase.jogo.excecao.VilaNaoEncontradaException;
import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.RegiaoBonus;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.modelo.VilaPrevia;
import com.example.loginbase.jogo.servico.GeradorMapaService.RegiaoGerada;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;
import com.example.loginbase.jogo.repositorio.LadrilhoJazidaRepository;
import com.example.loginbase.jogo.repositorio.RegiaoBonusRepository;
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
	private final FamiliaService familiaService;
	private final GeradorMapaService geradorMapa;
	private final RegiaoBonusRepository regiaoBonusRepository;
	private final VilaPreviaService previaService;
	private final BonusRegiaoService bonusRegiaoService;

	public VilaService(VilaRepository vilaRepository, RegiaoRepository regiaoRepository,
			LadrilhoJazidaRepository ladrilhoRepository, ConstrucaoRepository construcaoRepository,
			JogoTurnoRepository turnoRepository, UsuarioRepository usuarioRepository,
			GeradorJazidaService gerador, EstoqueService estoqueService, FamiliaService familiaService,
			GeradorMapaService geradorMapa, RegiaoBonusRepository regiaoBonusRepository,
			VilaPreviaService previaService, BonusRegiaoService bonusRegiaoService) {
		this.bonusRegiaoService = bonusRegiaoService;
		this.vilaRepository = vilaRepository;
		this.regiaoRepository = regiaoRepository;
		this.ladrilhoRepository = ladrilhoRepository;
		this.construcaoRepository = construcaoRepository;
		this.turnoRepository = turnoRepository;
		this.usuarioRepository = usuarioRepository;
		this.gerador = gerador;
		this.estoqueService = estoqueService;
		this.familiaService = familiaService;
		this.geradorMapa = geradorMapa;
		this.regiaoBonusRepository = regiaoBonusRepository;
		this.previaService = previaService;
	}

	/** Cria a vila a partir da prévia vigente (validações na ordem: vila, prévia, índices, conexo, Urbana). */
	@Transactional
	public Vila criarVila(Long usuarioId, UUID previaId, List<Integer> indices) {
		exigirSemVila(usuarioId);
		VilaPrevia previa = previaService.exigirVigente(usuarioId, previaId);
		Vila vila = criarVilaComSemente(usuarioId, previa.getSemente(), indices);
		previaService.remover(usuarioId);
		return vila;
	}

	/** Cria a vila com o mapa da semente informada, sem passar pela prévia (usado também pelos testes). */
	@Transactional
	public Vila criarVilaComSemente(Long usuarioId, long semente, List<Integer> indices) {
		exigirSemVila(usuarioId);
		validarSelecao(indices);
		List<RegiaoGerada> mapa = geradorMapa.gerar(semente);
		if (!geradorMapa.conectadas(indices)) {
			throw new RegiaoNaoAdjacenteException("As regiões escolhidas precisam ser vizinhas entre si");
		}
		int regiaoUrbana = indices.stream().filter(i -> mapa.get(i - 1).tipo() == TipoRegiao.URBANA).findFirst()
				.orElseThrow(() -> new UrbanaObrigatoriaException("Ao menos uma região deve ser Urbana"));

		int maiorTurno = turnoRepository.maiorNumero();
		int turnoCriacao = maiorTurno > 0 ? maiorTurno : 1;
		String nome = usuarioRepository.findById(usuarioId).map(u -> "Vila de " + u.getNome())
				.orElse("Vila");

		Vila vila = vilaRepository.save(new Vila(usuarioId, nome, semente, turnoCriacao));

		for (RegiaoGerada gerada : mapa) {
			Regiao regiao = new Regiao(vila.getId(), gerada.indice());
			regiao.setTipo(gerada.tipo());
			regiao.setPossuida(indices.contains(gerada.indice()));
			regiao = regiaoRepository.save(regiao);
			Long regiaoId = regiao.getId();
			regiaoBonusRepository.saveAll(gerada.bonus().stream()
					.map(b -> new RegiaoBonus(regiaoId, b.bonus(), b.posicao(), b.valor())).toList());
			if (regiao.isPossuida()) {
				ladrilhoRepository.saveAll(gerador.gerarLadrilhos(semente, gerada.indice(), regiaoId));
			}
		}

		estoqueService.inicializar(vila, ESTOQUE_INICIAL);

		List<Long> casaIds = new ArrayList<>();
		for (int x : X_CASAS_INICIAIS) {
			casaIds.add(construcaoRepository.save(novaCasa(vila.getId(), regiaoUrbana, x, 0)).getId());
		}
		familiaService.gerarFamiliasIniciais(vila, casaIds, semente);
		return vila;
	}

	private void exigirSemVila(Long usuarioId) {
		if (vilaRepository.existsByUsuarioId(usuarioId)) {
			throw new VilaJaExisteException("Usuário já possui uma vila");
		}
	}

	@Transactional(readOnly = true)
	public VilaResumoDTO resumo(Long usuarioId) {
		Vila vila = vilaRepository.findByUsuarioId(usuarioId)
				.orElseThrow(() -> new VilaNaoEncontradaException("Vila não encontrada"));
		return resumo(vila);
	}

	@Transactional(readOnly = true)
	public VilaResumoDTO resumo(Vila vila) {
		List<Regiao> doBanco = regiaoRepository.findAllByVilaId(vila.getId());
		Map<Long, List<RegiaoBonusDTO>> bonusPorRegiao = bonusRegiaoService
				.bonusDasRegioes(doBanco.stream().map(Regiao::getId).toList());
		List<VilaResumoDTO.Regiao> regioes = doBanco.stream()
				.sorted(Comparator.comparing(Regiao::getIndice))
				.map(r -> new VilaResumoDTO.Regiao(r.getIndice(), r.getTipo(), r.isPossuida(),
						bonusPorRegiao.getOrDefault(r.getId(), List.of())))
				.toList();
		Map<String, BigDecimal> estoque = new LinkedHashMap<>();
		estoqueService.listar(vila).forEach((r, q) -> estoque.put(r.name(), q));
		return new VilaResumoDTO(vila.getId(), vila.getNome(), vila.getSemente(), vila.getTurnoCriacao(),
				regioes, estoque, vila.isPopulacaoConfirmada(),
				bonusRegiaoService.bonusDaVila(vila.getId()));
	}

	private static void validarSelecao(List<Integer> indices) {
		boolean valido = indices != null && indices.size() == REGIOES_INICIAIS
				&& new HashSet<>(indices).size() == REGIOES_INICIAIS
				&& indices.stream().allMatch(i -> i != null && i >= 1 && i <= GradeRegioes.TOTAL);
		if (!valido) {
			throw new SelecaoInvalidaException("Escolha 3 regiões diferentes de 01 a 16");
		}
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
