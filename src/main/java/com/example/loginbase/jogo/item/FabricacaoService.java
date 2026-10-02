package com.example.loginbase.jogo.item;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores.Trabalhador;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoCatalogo;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.catalogo.JoiaCatalogo;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.turno.TurnoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FabricacaoService {

	private static final List<TipoConstrucao> OFICINAS = List.of(TipoConstrucao.FERRARIA,
			TipoConstrucao.ALFAIATARIA, TipoConstrucao.CARPINTARIA);

	private final ConstrucaoRepository construcaoRepository;
	private final FabricacaoRepository fabricacaoRepository;
	private final CidadaoRepository cidadaoRepository;
	private final ConsultaTrabalhadores consulta;
	private final PeArtesao peArtesao;
	private final CatalogoItens catalogo;
	private final EstoqueService estoqueService;
	private final TurnoService turnoService;

	@Transactional(readOnly = true)
	public OficinaDTO oficina(Vila vila, Long id) {
		Construcao c = construcaoDaVila(vila, id);
		verificarOficina(c);
		Profissao prof = profissao(c);
		List<ArtesaoDTO> artesaos = consulta.trabalhadores(c, vila).stream()
				.map(t -> new ArtesaoDTO(t.cidadao().getId(), t.cidadao().getNome(),
						peArtesao.peEfetivo(t.cidadao(), prof), t.eficiencia(),
						fabricacaoRepository.existsByArtesaoId(t.cidadao().getId())))
				.toList();
		return new OficinaDTO(c.getId(), c.getTipo().name(), c.getNivel().name(), c.getEstado().name(),
				RegrasFabricacao.nivelMaximo(c.getNivel()), artesaos);
	}

	@Transactional(readOnly = true)
	public List<FabricacaoDTO> fila(Vila vila, Long id) {
		Construcao c = construcaoDaVila(vila, id);
		verificarOficina(c);
		Map<Long, Trabalhador> trab = new LinkedHashMap<>();
		consulta.trabalhadores(c, vila).forEach(t -> trab.put(t.cidadao().getId(), t));
		return fabricacaoRepository.findByConstrucaoId(c.getId()).stream()
				.sorted(java.util.Comparator.comparing(Fabricacao::getId))
				.map(f -> dto(f, trab.get(f.getArtesaoId()))).toList();
	}

	@Transactional(readOnly = true)
	public List<ReceitaDTO> receitas(Vila vila, Long id) {
		Construcao c = construcaoDaVila(vila, id);
		verificarOficina(c);
		int max = RegrasFabricacao.nivelMaximo(c.getNivel());
		return catalogo.subtiposDa(c.getTipo()).stream().map(i -> {
			Map<Integer, Map<String, Integer>> custos = new LinkedHashMap<>();
			Map<Integer, Integer> pes = new LinkedHashMap<>();
			for (int l = 1; l <= max; l++) {
				custos.put(l, nomes(i.receita(l)));
				pes.put(l, RegrasFabricacao.peMinimo(l));
			}
			boolean exige = i instanceof JoiaCatalogo j && j.exigeAtributo();
			return new ReceitaDTO(i.subtipo().name(), i.subtipo().getNomeExibicao(), nomes(i.receitaBase()),
					custos, pes, exige);
		}).toList();
	}

	@Transactional
	public FabricacaoDTO criar(Vila vila, Long id, CriarFabricacaoRequest req) {
		Construcao c = construcaoDaVila(vila, id);
		verificarOficina(c);
		if (c.getEstado() != EstadoConstrucao.ATIVA) {
			throw erro("A oficina precisa estar ativa");
		}
		if (req == null || req.subtipo() == null) {
			throw erro("Informe o subtipo do item");
		}
		ItemCatalogado item = catalogo.de(req.subtipo())
				.orElseThrow(() -> erro("Subtipo inválido"));
		if (item.oficina() != c.getTipo()) {
			throw erro("Oficina incorreta. " + req.subtipo().getNomeExibicao() + " é fabricada na "
					+ nomeOficina(item.oficina()) + ".");
		}
		if (req.nivel() == null || req.nivel() < 1 || req.nivel() > 10) {
			throw erro("Nível deve estar entre 1 e 10");
		}
		int nivel = req.nivel();
		int max = RegrasFabricacao.nivelMaximo(c.getNivel());
		if (nivel > max) {
			throw erro("Nível máximo L" + max + " para esta oficina");
		}
		Cidadao artesao = validarArtesao(vila, c, req.artesaoId(), nivel, null);
		CodigoBonus atributo = validarAtributo(item, req.atributoEscolhido());

		debitarCusto(vila, item.receita(nivel));

		Fabricacao f = new Fabricacao(vila.getId(), c.getId(), artesao.getId(), req.subtipo(), nivel,
				RegrasFabricacao.pf(nivel), turnoService.getTurnoAtual().numero());
		f.setAtributoEscolhido(atributo);
		f = fabricacaoRepository.saveAndFlush(f);
		return dto(f, trabalhador(c, vila, artesao.getId()));
	}

	@Transactional
	public FabricacaoDTO reatribuir(Vila vila, Long fabricacaoId, Long artesaoId) {
		Fabricacao f = fabricacaoRepository.findById(fabricacaoId)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Fabricação não encontrada"));
		if (!f.getVilaId().equals(vila.getId())) {
			throw new JogoException(HttpStatus.NOT_FOUND, "Fabricação não encontrada");
		}
		Construcao c = construcaoDaVila(vila, f.getConstrucaoId());
		Cidadao artesao = validarArtesao(vila, c, artesaoId, f.getNivel(), f.getId());
		f.setArtesaoId(artesao.getId());
		f.setEstado(EstadoFabricacao.EM_ANDAMENTO);
		f = fabricacaoRepository.saveAndFlush(f);
		return dto(f, trabalhador(c, vila, artesao.getId()));
	}

	/** Verifica recurso a recurso (409 com mensagem detalhada) e debita o custo do estoque. */
	void debitarCusto(Vila vila, Map<Recurso, Integer> custo) {
		for (Map.Entry<Recurso, Integer> e : custo.entrySet()) {
			BigDecimal disp = estoqueService.quantidade(vila, e.getKey());
			if (disp.compareTo(BigDecimal.valueOf(e.getValue())) < 0) {
				throw new JogoException(HttpStatus.CONFLICT, e.getKey().getNomeExibicao() + " insuficiente ("
						+ disp.setScale(0, java.math.RoundingMode.DOWN).toPlainString() + " < " + e.getValue() + ")");
			}
		}
		Map<Recurso, BigDecimal> debito = new EnumMap<>(Recurso.class);
		custo.forEach((r, q) -> debito.put(r, BigDecimal.valueOf(q)));
		estoqueService.debitar(vila, debito, "Recursos insuficientes");
	}

	Cidadao validarArtesao(Vila vila, Construcao c, Long artesaoId, int nivel, Long fabricacaoIgnorada) {
		return validarArtesao(vila, c, artesaoId, nivel, fabricacaoIgnorada, "");
	}

	Cidadao validarArtesao(Vila vila, Construcao c, Long artesaoId, int nivel, Long fabricacaoIgnorada,
			String sufixoPe) {
		if (artesaoId == null) {
			throw erro("Informe o artesão");
		}
		Cidadao a = cidadaoRepository.findById(artesaoId)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Artesão não encontrado"));
		if (!a.getVilaId().equals(vila.getId())) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Cidadão pertence a outra vila");
		}
		if (!a.isVivo()) {
			throw erro("Artesão morto");
		}
		if (!c.getId().equals(a.getConstrucaoId())) {
			throw erro("Artesão não está alocado nesta oficina");
		}
		boolean ocupado = fabricacaoRepository.findByArtesaoId(artesaoId)
				.filter(o -> !o.getId().equals(fabricacaoIgnorada)).isPresent();
		if (ocupado) {
			throw erro("Artesão já está fabricando outro item");
		}
		int minimo = RegrasFabricacao.peMinimo(nivel);
		if (peArtesao.peEfetivo(a, profissao(c)) < minimo) {
			throw erro("PE efetivo mínimo " + minimo + " necessário" + sufixoPe);
		}
		return a;
	}

	private CodigoBonus validarAtributo(ItemCatalogado item, String atributo) {
		boolean exige = item instanceof JoiaCatalogo j && j.exigeAtributo();
		if (!exige) {
			if (atributo != null && !atributo.isBlank()) {
				throw erro("Este item não aceita atributo escolhido");
			}
			return null;
		}
		if (atributo == null || atributo.isBlank()) {
			throw erro("Anel exige a escolha de um atributo");
		}
		Caracteristica car;
		try {
			car = Caracteristica.valueOf(atributo.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw erro("Atributo inválido");
		}
		if (!JoiaCatalogo.caracteristicaPermitida(car)) {
			throw erro("Atributo inválido");
		}
		return CodigoBonus.valueOf(car.name());
	}

	Construcao construcaoDaVila(Vila vila, Long id) {
		return construcaoRepository.findById(id).filter(c -> c.getVilaId().equals(vila.getId()))
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Construção não encontrada"));
	}

	void verificarOficina(Construcao c) {
		if (!OFICINAS.contains(c.getTipo())) {
			throw erro("Construção não é uma oficina");
		}
	}

	private Profissao profissao(Construcao c) {
		return ConstrucaoCatalogo.profissoes(c.getTipo()).get(0);
	}

	Trabalhador trabalhador(Construcao c, Vila vila, Long cidadaoId) {
		return consulta.trabalhadores(c, vila).stream().filter(t -> t.cidadao().getId().equals(cidadaoId))
				.findFirst().orElse(null);
	}

	FabricacaoDTO dto(Fabricacao f, Trabalhador t) {
		Integer turnos = null;
		if (t != null && t.eficiencia() > 0) {
			double restante = f.getPfTotal() - f.getPfAtual().doubleValue();
			turnos = (int) Math.max(0, Math.ceil(restante / t.eficiencia() - 1e-9));
		}
		String nome = t != null ? t.cidadao().getNome()
				: cidadaoRepository.findById(f.getArtesaoId()).map(Cidadao::getNome).orElse(null);
		return new FabricacaoDTO(f.getId(), f.getSubtipo().name(), f.getNivel(), f.getArtesaoId(), nome,
				f.getPfAtual(), f.getPfTotal(), f.getEstado().name(), turnos, f.getItemId());
	}

	private static Map<String, Integer> nomes(Map<Recurso, Integer> m) {
		Map<String, Integer> r = new LinkedHashMap<>();
		m.forEach((k, v) -> r.put(k.name(), v));
		return r;
	}

	static String nomeOficina(TipoConstrucao t) {
		return switch (t) {
			case FERRARIA -> "Ferraria";
			case ALFAIATARIA -> "Alfaiataria";
			case CARPINTARIA -> "Carpintaria";
			default -> t.name();
		};
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}
}
