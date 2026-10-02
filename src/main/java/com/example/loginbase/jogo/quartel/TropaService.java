package com.example.loginbase.jogo.quartel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoCatalogo;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.modelo.Vila;

import lombok.RequiredArgsConstructor;

/** Formação e edição de tropas no quartel (tropas.md). Todo recurso de outra vila responde 404. */
@Service
@RequiredArgsConstructor
public class TropaService {

	private static final int NOME_MAX = 100;

	private final TropaRepository tropaRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final CidadaoRepository cidadaoRepository;
	private final CidadaoProfissaoRepository profissaoRepository;
	private final ItemRepository itemRepository;
	private final TropaValidator validator;

	@Transactional(readOnly = true)
	public QuartelDTO quartel(Vila vila, Long id) {
		Construcao q = quartelDaVila(vila, id);
		List<Tropa> tropas = tropasOrdenadas(q);
		long membros = contarMembros(tropas);
		return new QuartelDTO(q.getId(), q.getNivel().name(), q.getEstado().name(), instrutores(q).size(),
				ConstrucaoCatalogo.vagasInstrutorQuartel(q.getNivel()), ConstrucaoCatalogo.capacidadeQuartel(q.getNivel()),
				(int) membros, ConstrucaoCatalogo.maxTropasQuartel(q.getNivel()),
				tropas.stream().map(this::dto).toList());
	}

	@Transactional(readOnly = true)
	public List<GuerreiroDisponivelDTO> guerreirosDisponiveis(Vila vila, Long quartelId) {
		quartelDaVila(vila, quartelId);
		return cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId()).stream()
				.sorted(Comparator.comparing(Cidadao::getNome).thenComparing(Cidadao::getId))
				.map(c -> {
					int pe = peBase(c);
					Item arma = arma(c);
					var motivo = validator.motivoInelegivel(c, pe, arma != null);
					return new GuerreiroDisponivelDTO(c.getId(), c.getNome(), c.getIdadeAnos(), pe,
							arma == null ? null : arma.getSubtipo().getNomeExibicao(), c.getEstado().name(),
							motivo.isEmpty(), motivo.orElse(null));
				}).toList();
	}

	@Transactional
	public TropaDTO criar(Vila vila, Long quartelId, CriarTropaRequest req) {
		Construcao q = quartelDaVila(vila, quartelId);
		String nome = req == null || req.nome() == null ? "" : req.nome().trim();
		if (nome.isEmpty()) {
			throw erro("Informe o nome da tropa");
		}
		if (nome.length() > NOME_MAX) {
			throw erro("Nome da tropa deve ter no máximo " + NOME_MAX + " caracteres");
		}
		validator.validarQuartel(q, instrutores(q).size());
		List<Tropa> existentes = tropaRepository.findByQuartelId(q.getId());
		validator.validarLimiteTropas(q.getNivel(), existentes.size());
		if (tropaRepository.existsByVilaIdAndNome(vila.getId(), nome)) {
			throw erro("Já existe uma tropa com este nome");
		}
		List<MembroTropaRequest> pedidos = req.membros() == null ? List.of() : req.membros();
		List<Cidadao> cidadaos = validarNovosMembros(vila, q, existentes, pedidos);
		Tropa tropa = tropaRepository.saveAndFlush(new Tropa(vila.getId(), q.getId(), nome));
		for (int i = 0; i < cidadaos.size(); i++) {
			vincular(cidadaos.get(i), tropa, pedidos.get(i).posicao());
		}
		return dto(tropa);
	}

	@Transactional(readOnly = true)
	public TropaDTO tropa(Vila vila, Long id) {
		return dto(tropaDaVila(vila, id));
	}

	@Transactional
	public TropaDTO adicionarMembro(Vila vila, Long tropaId, MembroTropaRequest req) {
		Tropa tropa = tropaDaVila(vila, tropaId);
		exigirAquartelada(tropa);
		Construcao q = construcaoRepository.findById(tropa.getQuartelId()).orElseThrow();
		validator.validarQuartel(q, instrutores(q).size());
		List<Cidadao> cid = validarNovosMembros(vila, q, tropaRepository.findByQuartelId(q.getId()),
				List.of(req == null ? new MembroTropaRequest(null, null) : req));
		vincular(cid.get(0), tropa, req.posicao());
		return dto(tropa);
	}

	@Transactional
	public TropaDTO removerMembro(Vila vila, Long tropaId, Long cidadaoId) {
		Tropa tropa = tropaDaVila(vila, tropaId);
		exigirAquartelada(tropa);
		Cidadao c = cidadaoRepository.findById(cidadaoId).filter(x -> tropa.getId().equals(x.getTropaId()))
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Membro não encontrado na tropa"));
		c.setTropaId(null);
		c.setPosicaoTropa(null);
		cidadaoRepository.saveAndFlush(c);
		return dto(tropa);
	}

	@Transactional
	public void desfazer(Vila vila, Long tropaId) {
		Tropa tropa = tropaDaVila(vila, tropaId);
		exigirAquartelada(tropa);
		for (Cidadao c : cidadaoRepository.findByTropaId(tropa.getId())) {
			c.setTropaId(null);
			c.setPosicaoTropa(null);
			cidadaoRepository.save(c);
		}
		cidadaoRepository.flush();
		tropaRepository.delete(tropa);
		tropaRepository.flush();
	}

	/** Valida todos os pedidos (inclusive duplicados e capacidade total) sem alterar nada. */
	private List<Cidadao> validarNovosMembros(Vila vila, Construcao q, List<Tropa> tropasDoQuartel,
			List<MembroTropaRequest> pedidos) {
		validator.validarCapacidade(q.getNivel(), contarMembros(tropasDoQuartel), pedidos.size());
		Set<Long> vistos = new HashSet<>();
		List<Cidadao> resultado = new ArrayList<>();
		for (MembroTropaRequest p : pedidos) {
			if (p == null || p.cidadaoId() == null) {
				throw erro("Informe o cidadão");
			}
			if (p.posicao() == null) {
				throw erro("Informe a posição (FRENTE ou RETAGUARDA)");
			}
			Cidadao c = cidadaoRepository.findById(p.cidadaoId()).filter(x -> x.getVilaId().equals(vila.getId()))
					.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Cidadão não encontrado"));
			if (!vistos.add(c.getId())) {
				throw erro("Cidadão já está em tropa");
			}
			validator.validarMembro(c, peBase(c), arma(c) != null);
			resultado.add(c);
		}
		return resultado;
	}

	private void vincular(Cidadao c, Tropa tropa, PosicaoTropa posicao) {
		c.setTropaId(tropa.getId());
		c.setPosicaoTropa(posicao);
		cidadaoRepository.saveAndFlush(c);
	}

	private TropaDTO dto(Tropa t) {
		List<MembroTropaDTO> membros = cidadaoRepository.findByTropaId(t.getId()).stream()
				.sorted(Comparator.comparing((Cidadao c) -> c.getPosicaoTropa()).thenComparing(Cidadao::getId))
				.map(c -> new MembroTropaDTO(c.getId(), c.getNome(), c.getPosicaoTropa(), c.getIdadeAnos(),
						c.getEstado().name(), c.getXpGuerreiro(), peBase(c)))
				.toList();
		return new TropaDTO(t.getId(), t.getNome(), t.getEstado().name(), t.getQuartelId(), t.getMasmorraId(),
				t.getRegiaoDestino(), t.getTurnosViagem(), t.getTurnosRestantes(), membros.size(), membros);
	}

	private List<Tropa> tropasOrdenadas(Construcao q) {
		return tropaRepository.findByQuartelId(q.getId()).stream().sorted(Comparator.comparing(Tropa::getId)).toList();
	}

	private long contarMembros(List<Tropa> tropas) {
		return tropas.stream().mapToLong(t -> cidadaoRepository.countByTropaId(t.getId())).sum();
	}

	/** Instrutor = Guerreiro vivo e saudável alocado no quartel. */
	private List<Cidadao> instrutores(Construcao q) {
		return cidadaoRepository.findByConstrucaoIdAndVivoTrue(q.getId()).stream()
				.filter(c -> c.getProfissaoTrabalho() == Profissao.GUERREIRO && c.getEstado() == EstadoCidadao.SAUDAVEL)
				.toList();
	}

	private int peBase(Cidadao c) {
		return profissaoRepository.findByCidadaoIdAndProfissao(c.getId(), Profissao.GUERREIRO)
				.map(CidadaoProfissao::getPontosBase).orElse(0);
	}

	private Item arma(Cidadao c) {
		return itemRepository.findByCidadaoIdAndSlot(c.getId(), SlotEquipamento.ARMA).orElse(null);
	}

	private Construcao quartelDaVila(Vila vila, Long id) {
		return construcaoRepository.findByIdAndVilaId(id, vila.getId()).filter(c -> c.getTipo() == TipoConstrucao.QUARTEL)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Quartel não encontrado"));
	}

	private Tropa tropaDaVila(Vila vila, Long id) {
		return tropaRepository.findByIdAndVilaId(id, vila.getId())
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Tropa não encontrada"));
	}

	private static void exigirAquartelada(Tropa t) {
		if (t.getEstado() != EstadoTropa.AQUARTELADA) {
			throw erro("Tropa em expedição não pode ser alterada");
		}
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}

}
