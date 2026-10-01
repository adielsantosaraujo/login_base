package com.example.loginbase.jogo.comercio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.CalculadoraPeEfetivo;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores.Trabalhador;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Compra e venda de recursos com o mercador NPC. Exige Mercado ATIVA; o PE do melhor Comerciante alocado nos
 * Mercados define o preço; o volume do turno (compras + vendas) soma todos os Mercados ativos:
 * {@code 20 x soma(eficiência dos Comerciantes)} (a eficiência já inclui o multiplicador do nível).
 */
@Service
public class MercadoService {

	static final int VOLUME_POR_EFICIENCIA = 20;

	private final ConstrucaoRepository construcaoRepository;
	private final ConsultaTrabalhadores consultaTrabalhadores;
	private final CalculadoraPeEfetivo calculadoraPeEfetivo;
	private final CidadaoProfissaoRepository cidadaoProfissaoRepository;
	private final OrdemComercioRepository ordemRepository;
	private final EstoqueService estoqueService;
	private final JogoTurnoRepository turnoRepository;
	private final RegistroEventoTurnoService eventoService;

	public MercadoService(ConstrucaoRepository construcaoRepository, ConsultaTrabalhadores consultaTrabalhadores,
			CalculadoraPeEfetivo calculadoraPeEfetivo, CidadaoProfissaoRepository cidadaoProfissaoRepository,
			OrdemComercioRepository ordemRepository, EstoqueService estoqueService,
			JogoTurnoRepository turnoRepository, RegistroEventoTurnoService eventoService) {
		this.construcaoRepository = construcaoRepository;
		this.consultaTrabalhadores = consultaTrabalhadores;
		this.calculadoraPeEfetivo = calculadoraPeEfetivo;
		this.cidadaoProfissaoRepository = cidadaoProfissaoRepository;
		this.ordemRepository = ordemRepository;
		this.estoqueService = estoqueService;
		this.turnoRepository = turnoRepository;
		this.eventoService = eventoService;
	}

	/** Executa a ordem imediatamente, consumindo o volume do turno corrente. */
	@Transactional
	public OrdemResultadoDTO executarOrdem(Vila vila, Recurso recurso, TipoOrdem tipo, int quantidade) {
		if (recurso == null || tipo == null) {
			throw erro(HttpStatus.BAD_REQUEST, "Recurso e tipo são obrigatórios");
		}
		if (quantidade <= 0) {
			throw erro(HttpStatus.BAD_REQUEST, "Quantidade deve ser maior que zero");
		}
		if (!CatalogoPrecos.negociavel(recurso)) {
			throw erro(HttpStatus.BAD_REQUEST, "Recurso não negociável");
		}
		Condicoes cond = condicoes(vila);
		if (!cond.mercadoAtivo()) {
			throw erro(HttpStatus.BAD_REQUEST, "Mercado não disponível");
		}
		int turno = turnoRepository.maiorNumero();
		long usado = ordemRepository.volumeDoTurno(vila.getId(), turno);
		if (usado + quantidade > cond.volumeMaximo()) {
			throw erro(HttpStatus.BAD_REQUEST, "Volume diário excedido");
		}
		BigDecimal qtd = BigDecimal.valueOf(quantidade);
		BigDecimal unitario = tipo == TipoOrdem.VENDA ? CatalogoPrecos.precoVenda(recurso, cond.pe())
				: CatalogoPrecos.precoCompra(recurso, cond.pe());
		BigDecimal total = unitario.multiply(qtd);

		if (tipo == TipoOrdem.VENDA) {
			estoqueService.debitar(vila, Map.of(recurso, qtd), "Recurso insuficiente");
			estoqueService.creditar(vila, Recurso.OURO, total);
		} else {
			BigDecimal capacidade = estoqueService.calcularCapacidadeTotal(vila).get(recurso);
			if (estoqueService.quantidade(vila, recurso).add(qtd).compareTo(capacidade) > 0) {
				throw erro(HttpStatus.BAD_REQUEST, "Capacidade de armazenamento excedida");
			}
			estoqueService.debitar(vila, Map.of(Recurso.OURO, total), "Ouro insuficiente");
			estoqueService.creditar(vila, recurso, qtd);
		}

		ordemRepository.save(new OrdemComercio(vila.getId(), turno, tipo, recurso, quantidade, unitario, total));
		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("recurso", recurso.name());
		dados.put("quantidade", quantidade);
		dados.put("precoUnitario", unitario);
		dados.put("ouroTotal", total);
		dados.put("pe", cond.pe());
		boolean venda = tipo == TipoOrdem.VENDA;
		eventoService.registrar(vila, turno, venda ? TipoEventoTurno.MERCADO_VENDA : TipoEventoTurno.MERCADO_COMPRA,
				(venda ? "Vendeu " : "Comprou ") + quantidade + " " + recurso.getNomeExibicao() + " por "
						+ total.toPlainString() + " Ouro",
				dados);
		return new OrdemResultadoDTO(true, tipo.name(), recurso.name(), quantidade, unitario, total,
				estoqueService.quantidade(vila, recurso), estoqueService.quantidade(vila, Recurso.OURO),
				usado + quantidade, cond.volumeMaximo());
	}

	/** Preços atuais de todos os recursos negociáveis, com volume do turno (para a tela do Mercado). */
	@Transactional(readOnly = true)
	public PrecosMercadoDTO precos(Vila vila) {
		Condicoes cond = condicoes(vila);
		long usado = ordemRepository.volumeDoTurno(vila.getId(), turnoRepository.maiorNumero());
		List<PrecosMercadoDTO.LinhaPreco> linhas = new ArrayList<>();
		for (Recurso r : Recurso.values()) {
			if (CatalogoPrecos.negociavel(r)) {
				linhas.add(new PrecosMercadoDTO.LinhaPreco(r.name(), r.getNomeExibicao(), CatalogoPrecos.precoBase(r),
						CatalogoPrecos.precoVenda(r, cond.pe()), CatalogoPrecos.precoCompra(r, cond.pe())));
			}
		}
		return new PrecosMercadoDTO(cond.mercadoAtivo(), cond.pe(), cond.volumeMaximo(), usado,
				Math.max(0, cond.volumeMaximo() - usado), linhas);
	}

	/** Histórico de ordens da vila (mais recentes primeiro). */
	@Transactional(readOnly = true)
	public List<OrdemComercio> historico(Vila vila) {
		return ordemRepository.findByVilaIdOrderByIdDesc(vila.getId());
	}

	private Condicoes condicoes(Vila vila) {
		List<Construcao> mercados = construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.ATIVA)
				.stream().filter(c -> c.getTipo() == TipoConstrucao.MERCADO).toList();
		int pe = 0;
		double eficiencia = 0;
		for (Construcao m : mercados) {
			for (Trabalhador t : consultaTrabalhadores.trabalhadores(m, vila)) {
				if (t.profissao() != Profissao.COMERCIANTE) {
					continue;
				}
				eficiencia += t.eficiencia();
				int peBase = cidadaoProfissaoRepository.findByCidadaoIdAndProfissao(t.cidadao().getId(), t.profissao())
						.map(CidadaoProfissao::getPontosBase).orElse(0);
				pe = Math.max(pe, calculadoraPeEfetivo.calcular(t.cidadao(), t.profissao(), peBase));
			}
		}
		long volume = (long) Math.floor(VOLUME_POR_EFICIENCIA * eficiencia + 1e-9);
		return new Condicoes(!mercados.isEmpty(), pe, volume);
	}

	private record Condicoes(boolean mercadoAtivo, int pe, long volumeMaximo) {
	}

	private static JogoException erro(HttpStatus status, String msg) {
		return new JogoException(status, msg);
	}

}
