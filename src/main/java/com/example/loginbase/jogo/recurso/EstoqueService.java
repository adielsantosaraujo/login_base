package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.excecao.RecursosInsuficientesException;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.FonteCapacidadeArmazens.ArmazemAtivo;

@Service
public class EstoqueService {

	/** Capacidade do Ouro (sem limite superior). */
	public static final BigDecimal CAPACIDADE_ILIMITADA = BigDecimal.valueOf(Long.MAX_VALUE);

	static final BigDecimal CAPACIDADE_BASE = BigDecimal.valueOf(500);
	static final BigDecimal TETO_EFICIENCIA = new BigDecimal("1.5");

	private final EstoqueRepository repository;
	private final FonteCapacidadeArmazens fonteArmazens;

	public EstoqueService(EstoqueRepository repository, FonteCapacidadeArmazens fonteArmazens) {
		this.repository = repository;
		this.fonteArmazens = fonteArmazens;
	}

	/** Cria o estoque da vila com todos os recursos (os não informados começam em 0). */
	@Transactional
	public void inicializar(Vila vila, Map<Recurso, BigDecimal> iniciais) {
		Map<Recurso, Estoque> existentes = mapaPorRecurso(vila);
		for (Recurso r : Recurso.values()) {
			BigDecimal q = escala(iniciais.getOrDefault(r, BigDecimal.ZERO));
			Estoque e = existentes.get(r);
			if (e == null) {
				repository.save(new Estoque(vila.getId(), r, q));
			} else {
				e.setQuantidade(q);
			}
		}
	}

	@Transactional(readOnly = true)
	public BigDecimal quantidade(Vila vila, Recurso recurso) {
		return repository.findByVilaIdAndRecurso(vila.getId(), recurso).map(Estoque::getQuantidade)
				.orElse(escala(BigDecimal.ZERO));
	}

	/** Quantidade de todos os recursos (ausentes = 0). */
	@Transactional(readOnly = true)
	public Map<Recurso, BigDecimal> listar(Vila vila) {
		Map<Recurso, Estoque> existentes = mapaPorRecurso(vila);
		Map<Recurso, BigDecimal> resultado = new EnumMap<>(Recurso.class);
		for (Recurso r : Recurso.values()) {
			Estoque e = existentes.get(r);
			resultado.put(r, e == null ? escala(BigDecimal.ZERO) : e.getQuantidade());
		}
		return resultado;
	}

	/** Soma a quantidade ao estoque (sem aplicar limite; isso ocorre no passo 4 do turno). */
	@Transactional
	public void creditar(Vila vila, Recurso recurso, BigDecimal quantidade) {
		if (quantidade.signum() < 0) {
			throw new IllegalArgumentException("Quantidade a creditar não pode ser negativa");
		}
		List<Estoque> linhas = repository.findParaAtualizar(vila.getId(), List.of(recurso));
		if (linhas.isEmpty()) {
			repository.save(new Estoque(vila.getId(), recurso, escala(quantidade)));
		} else {
			Estoque e = linhas.get(0);
			e.setQuantidade(e.getQuantidade().add(escala(quantidade)));
		}
	}

	/** Debita todos os recursos ou nenhum; lança {@link RecursosInsuficientesException} se faltar. */
	@Transactional
	public void debitar(Vila vila, Map<Recurso, BigDecimal> custos) {
		debitar(vila, custos, RecursosInsuficientesException.MENSAGEM_PADRAO);
	}

	@Transactional
	public void debitar(Vila vila, Map<Recurso, BigDecimal> custos, String mensagemInsuficiente) {
		if (custos.isEmpty()) {
			return;
		}
		Map<Recurso, Estoque> linhas = repository.findParaAtualizar(vila.getId(), custos.keySet()).stream()
				.collect(Collectors.toMap(Estoque::getRecurso, Function.identity()));
		for (Map.Entry<Recurso, BigDecimal> c : custos.entrySet()) {
			Estoque e = linhas.get(c.getKey());
			BigDecimal disponivel = e == null ? BigDecimal.ZERO : e.getQuantidade();
			if (disponivel.compareTo(c.getValue()) < 0) {
				throw new RecursosInsuficientesException(mensagemInsuficiente);
			}
		}
		for (Map.Entry<Recurso, BigDecimal> c : custos.entrySet()) {
			if (c.getValue().signum() == 0) {
				continue;
			}
			Estoque e = linhas.get(c.getKey());
			e.setQuantidade(e.getQuantidade().subtract(escala(c.getValue())));
		}
	}

	/**
	 * Capacidade máxima por recurso: base 500 mais, para cada Armazém ativo com o mínimo de Carregadores
	 * (N1 1, N2 2, N3 4), o extra (N1 500, N2 1.500, N3 4.000) x min(1,5; eficiência média).
	 * O Ouro é ilimitado.
	 */
	@Transactional(readOnly = true)
	public Map<Recurso, BigDecimal> calcularCapacidadeTotal(Vila vila) {
		BigDecimal total = CAPACIDADE_BASE;
		for (ArmazemAtivo a : fonteArmazens.armazensAtivos(vila)) {
			if (a.carregadores() >= minimoCarregadores(a.nivel())) {
				BigDecimal eficiencia = a.eficienciaMedia().min(TETO_EFICIENCIA);
				total = total.add(extra(a.nivel()).multiply(eficiencia));
			}
		}
		total = total.setScale(2, RoundingMode.DOWN);
		Map<Recurso, BigDecimal> capacidade = new EnumMap<>(Recurso.class);
		for (Recurso r : Recurso.values()) {
			capacidade.put(r, r == Recurso.OURO ? CAPACIDADE_ILIMITADA : total);
		}
		return capacidade;
	}

	/**
	 * Passo 4 do turno: reduz ao limite o estoque excedente e devolve a perda por recurso
	 * (só os recursos que perderam algo).
	 */
	@Transactional
	public Map<Recurso, BigDecimal> aplicarLimiteArmazenamento(Vila vila, int turno) {
		Map<Recurso, BigDecimal> capacidade = calcularCapacidadeTotal(vila);
		Map<Recurso, BigDecimal> perdas = new EnumMap<>(Recurso.class);
		for (Estoque e : repository.findByVilaId(vila.getId())) {
			BigDecimal limite = capacidade.get(e.getRecurso());
			if (e.getQuantidade().compareTo(limite) > 0) {
				perdas.put(e.getRecurso(), e.getQuantidade().subtract(limite));
				e.setQuantidade(limite);
			}
		}
		return perdas;
	}

	private Map<Recurso, Estoque> mapaPorRecurso(Vila vila) {
		Map<Recurso, Estoque> mapa = new EnumMap<>(Recurso.class);
		repository.findByVilaId(vila.getId()).forEach(e -> mapa.put(e.getRecurso(), e));
		return mapa;
	}

	private static BigDecimal escala(BigDecimal valor) {
		return valor.setScale(2, RoundingMode.HALF_UP);
	}

	private static int minimoCarregadores(NivelConstrucao nivel) {
		return switch (nivel) {
			case N1 -> 1;
			case N2 -> 2;
			case N3 -> 4;
		};
	}

	private static BigDecimal extra(NivelConstrucao nivel) {
		return switch (nivel) {
			case N1 -> BigDecimal.valueOf(500);
			case N2 -> BigDecimal.valueOf(1500);
			case N3 -> BigDecimal.valueOf(4000);
		};
	}

}
