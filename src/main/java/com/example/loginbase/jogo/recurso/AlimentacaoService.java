package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.MorteService;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.quartel.EstadoTropa;
import com.example.loginbase.jogo.quartel.Tropa;
import com.example.loginbase.jogo.quartel.TropaRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Passo 3 do turno: consumo de alimentos (Refeição, Grãos, Carne, nessa ordem), bem alimentada, fome e
 * morte por fome. Com comida insuficiente, são alimentados primeiro os menores de idade e depois por id;
 * quem não recebe a porção inteira fica faminto.
 */
@Service
public class AlimentacaoService {

	public static final int IDADE_ADULTO_ANOS = 14;
	public static final int TURNOS_FOME_PERDA_VIT = 3;
	static final BigDecimal CONSUMO_ADULTO = BigDecimal.ONE;
	static final BigDecimal CONSUMO_MENOR = new BigDecimal("0.5");
	static final List<Recurso> ORDEM = List.of(Recurso.REFEICAO, Recurso.GRAOS, Recurso.CARNE);

	private final CidadaoRepository cidadaoRepository;
	private final VilaRepository vilaRepository;
	private final EstoqueService estoqueService;
	private final MorteService morteService;
	private final RegistroEventoTurnoService registro;
	private final TropaRepository tropaRepository;

	public AlimentacaoService(CidadaoRepository cidadaoRepository, VilaRepository vilaRepository,
			EstoqueService estoqueService, MorteService morteService, RegistroEventoTurnoService registro,
			TropaRepository tropaRepository) {
		this.cidadaoRepository = cidadaoRepository;
		this.vilaRepository = vilaRepository;
		this.estoqueService = estoqueService;
		this.morteService = morteService;
		this.registro = registro;
		this.tropaRepository = tropaRepository;
	}

	static BigDecimal demanda(Cidadao c) {
		return c.getIdadeAnos() >= IDADE_ADULTO_ANOS ? CONSUMO_ADULTO : CONSUMO_MENOR;
	}

	@Transactional
	public void processarConsumoAlimentacao(Vila vila, int turno) {
		// quem está em tropa em viagem (ida/volta) já pagou a comida no envio: não consome no passo 3
		Set<Long> tropasEmViagem = tropaRepository.findByVilaId(vila.getId()).stream()
				.filter(t -> t.getEstado() != EstadoTropa.AQUARTELADA).map(Tropa::getId).collect(Collectors.toSet());
		List<Cidadao> vivos = new ArrayList<>(cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId()));
		vivos.removeIf(c -> c.getTropaId() != null && tropasEmViagem.contains(c.getTropaId()));
		vivos.sort(Comparator.comparing((Cidadao c) -> c.getIdadeAnos() >= IDADE_ADULTO_ANOS)
				.thenComparing(Cidadao::getId));

		BigDecimal demandado = BigDecimal.ZERO;
		for (Cidadao c : vivos) {
			demandado = demandado.add(demanda(c));
		}

		Map<Recurso, BigDecimal> estoque = estoqueService.listar(vila);
		BigDecimal disponivel = BigDecimal.ZERO;
		for (Recurso r : ORDEM) {
			disponivel = disponivel.add(estoque.get(r));
		}

		// distribuição determinística da comida disponível
		BigDecimal restante = disponivel;
		BigDecimal consumido = BigDecimal.ZERO;
		List<Cidadao> famintos = new ArrayList<>();
		for (Cidadao c : vivos) {
			BigDecimal d = demanda(c);
			if (restante.compareTo(d) >= 0) {
				restante = restante.subtract(d);
				consumido = consumido.add(d);
				c.setFamintoTurnos(0);
			} else {
				famintos.add(c);
			}
		}

		// débito na ordem Refeição -> Grãos -> Carne
		Map<Recurso, BigDecimal> debitos = distribuirDebito(estoque, consumido);
		estoqueService.debitar(vila, debitos);

		BigDecimal refeicao = debitos.getOrDefault(Recurso.REFEICAO, BigDecimal.ZERO);
		boolean bem = demandado.signum() > 0
				&& refeicao.multiply(BigDecimal.valueOf(2)).compareTo(demandado) >= 0;
		vila.setBemAlimentada(bem);
		vilaRepository.save(vila);

		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("demandado", demandado);
		dados.put("consumido", consumido);
		for (Recurso r : ORDEM) {
			dados.put(r.name(), debitos.getOrDefault(r, BigDecimal.ZERO));
		}
		registro.registrar(vila, turno, TipoEventoTurno.ALIMENTOS_CONSUMIDOS,
				"Consumo de alimentos: " + fmt(consumido) + " de " + fmt(demandado) + ".", dados);
		if (bem) {
			registro.registrar(vila, turno, TipoEventoTurno.BEM_ALIMENTADA,
					"A vila está bem alimentada (refeições cobriram ao menos 50% do consumo).",
					Map.of("refeicoes", refeicao, "demandado", demandado));
		}

		for (Cidadao c : famintos) {
			c.setFamintoTurnos(c.getFamintoTurnos() + 1);
			boolean perdeVit = c.getFamintoTurnos() >= TURNOS_FOME_PERDA_VIT;
			if (perdeVit) {
				c.setVit(Math.max(0, c.getVit() - 1));
			}
			cidadaoRepository.save(c);
			Map<String, Object> d = new LinkedHashMap<>();
			d.put("cidadaoId", c.getId());
			d.put("nome", c.getNome());
			d.put("famintoTurnos", c.getFamintoTurnos());
			d.put("vit", c.getVit());
			registro.registrar(vila, turno, TipoEventoTurno.FOME,
					c.getNome() + " passou fome (" + c.getFamintoTurnos() + " turno(s)).", d);
			if (perdeVit && c.getVit() <= 0) {
				morteService.morrer(vila, c, "FOME", turno);
			}
		}
		for (Cidadao c : vivos) {
			if (!famintos.contains(c)) {
				cidadaoRepository.save(c);
			}
		}
	}

	/** Soma de Refeição + Grãos + Carne disponível na vila. */
	@Transactional(readOnly = true)
	public BigDecimal alimentosDisponiveis(Vila vila) {
		Map<Recurso, BigDecimal> estoque = estoqueService.listar(vila);
		BigDecimal total = BigDecimal.ZERO;
		for (Recurso r : ORDEM) {
			total = total.add(estoque.get(r));
		}
		return total;
	}

	/**
	 * Debita a quantidade de alimentos na ordem Refeição -> Grãos -> Carne (usado pelo envio de expedições).
	 * Comida insuficiente: 400 e nada é debitado.
	 */
	@Transactional
	public Map<Recurso, BigDecimal> debitarAlimentos(Vila vila, int quantidade) {
		BigDecimal qtd = BigDecimal.valueOf(quantidade);
		Map<Recurso, BigDecimal> estoque = estoqueService.listar(vila);
		if (alimentosDisponiveis(vila).compareTo(qtd) < 0) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Comida insuficiente para a expedição");
		}
		Map<Recurso, BigDecimal> debitos = distribuirDebito(estoque, qtd);
		estoqueService.debitar(vila, debitos);
		return debitos;
	}

	private static Map<Recurso, BigDecimal> distribuirDebito(Map<Recurso, BigDecimal> estoque, BigDecimal quantidade) {
		Map<Recurso, BigDecimal> debitos = new EnumMap<>(Recurso.class);
		BigDecimal falta = quantidade;
		for (Recurso r : ORDEM) {
			BigDecimal tira = estoque.get(r).min(falta);
			if (tira.signum() > 0) {
				debitos.put(r, tira);
				falta = falta.subtract(tira);
			}
		}
		return debitos;
	}

	private static String fmt(BigDecimal v) {
		return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

}
