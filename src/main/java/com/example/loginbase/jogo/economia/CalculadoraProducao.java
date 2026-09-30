package com.example.loginbase.jogo.economia;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.CurvaNiveis;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoRecurso;

/**
 * Classe pura (sem estado mutável) que calcula taxas de produção de
 * recursos por hora, capacidade máxima de armazenamento e avança estoques de
 * uma vila ao longo do tempo, respeitando a velocidade configurada do jogo e
 * o esgotamento de capacidade.
 *
 * <p>Não depende da entidade {@code Vila} (integração feita em
 * {@code VilaService}): recebe os níveis de prédio e os cultivos ativos como
 * dados simples, o que a mantém pura e testável isoladamente.
 */
public final class CalculadoraProducao {

	private static final long MILISSEGUNDOS_POR_HORA = 3_600_000L;
	private static final long MILESIMOS_POR_UNIDADE = 1000L;

	private final int velocidade;
	private final CurvaNiveis curva;

	/**
	 * @param velocidade multiplicador de velocidade do jogo ({@code app.jogo.velocidade}), &gt;= 1
	 * @param curva      curva de níveis configurada (não nula)
	 */
	public CalculadoraProducao(int velocidade, CurvaNiveis curva) {
		if (velocidade < 1) {
			throw new IllegalArgumentException("Velocidade inválida: " + velocidade);
		}
		this.velocidade = velocidade;
		this.curva = Objects.requireNonNull(curva, "curva de níveis não pode ser nula");
	}

	public int velocidade() {
		return velocidade;
	}

	/**
	 * Taxa de produção por hora (unidades inteiras, sem milésimos e sem o
	 * multiplicador de velocidade) de cada recurso:
	 * <ul>
	 * <li>{@code COMIDA}: soma da produção/h de cada cultivo ativo nos canteiros da fazenda;</li>
	 * <li>{@code MADEIRA}: {@code SERRARIA.nivel × 30};</li>
	 * <li>{@code PEDRA}: {@code PEDREIRA.nivel × 20};</li>
	 * <li>{@code FERRO}: {@code MINA_FERRO.nivel × 10}.</li>
	 * </ul>
	 *
	 * @param niveisPredio      níveis atuais dos prédios da vila (prédios ausentes/nível 0 não produzem)
	 * @param cultivosCanteiros cultivo ativo em cada canteiro ocupado da fazenda
	 */
	public Map<TipoRecurso, Long> taxaHoraPorRecurso(Map<TipoPredio, Integer> niveisPredio,
			List<Cultivo> cultivosCanteiros) {
		Map<TipoRecurso, Long> taxas = new EnumMap<>(TipoRecurso.class);
		long producaoComida = cultivosCanteiros.stream().mapToLong(Cultivo::producaoComidaPorHora).sum();
		taxas.put(TipoRecurso.COMIDA, producaoComida);
		taxas.put(TipoRecurso.MADEIRA, producaoPredio(niveisPredio, TipoPredio.SERRARIA));
		taxas.put(TipoRecurso.PEDRA, producaoPredio(niveisPredio, TipoPredio.PEDREIRA));
		taxas.put(TipoRecurso.FERRO, producaoPredio(niveisPredio, TipoPredio.MINA_FERRO));
		return taxas;
	}

	private long producaoPredio(Map<TipoPredio, Integer> niveisPredio, TipoPredio predio) {
		int nivel = niveisPredio.getOrDefault(predio, 0);
		return nivel <= 0 ? 0L : predio.producaoAdicionalPorHora(nivel);
	}

	/**
	 * Capacidade máxima de armazenamento por recurso, em milésimos, para o
	 * nível de armazém informado: {@code 500 × 2^(nivel-1) × 1000}.
	 */
	public long capacidadeMaxima(int nivelArmazem) {
		return Math.multiplyExact(TipoRecurso.capacidadeArmazem(nivelArmazem, curva), MILESIMOS_POR_UNIDADE);
	}

	/**
	 * Avança o estoque de {@code dataAnterior} até {@code dataAtual}, aplicando as taxas
	 * horárias informadas (sem o multiplicador de velocidade, aplicado aqui) e respeitando a
	 * capacidade máxima (igual para todos os recursos). Nunca ultrapassa a capacidade e não
	 * altera recursos que já estejam nela ou acima dela.
	 *
	 * <p>Algoritmo: {@code dtMs = dataAtual − dataAnterior}; para cada recurso,
	 * {@code ganho (milésimos) = taxa (unidades/h) × velocidade × dtMs × 1000 / 3_600_000}
	 * (ms → horas, arredondado para baixo, já convertido para milésimos); novo valor =
	 * {@code min(capacidade, estoque + ganho)} se o estoque atual for menor que a capacidade,
	 * senão o estoque permanece inalterado. Sem overflow: se o produto não couber em {@code long}
	 * (ou o ganho alcançar o espaço restante), o recurso satura na capacidade.
	 *
	 * @param estoqueAtual         estoque no instante {@code dataAnterior}
	 * @param taxasHoraPorRecurso  taxas/h por recurso (ver {@link #taxaHoraPorRecurso}), sem velocidade aplicada
	 * @param capacidade           capacidade máxima por recurso, em milésimos (ver {@link #capacidadeMaxima})
	 * @param dataAnterior         instante do estoque informado
	 * @param dataAtual            instante até o qual produzir
	 * @return novo estoque com os ganhos aplicados; o mesmo estoque se {@code dataAtual <= dataAnterior}
	 */
	public Estoque produzirAte(Estoque estoqueAtual, Map<TipoRecurso, Long> taxasHoraPorRecurso, long capacidade,
			Instant dataAnterior, Instant dataAtual) {
		long dtMs = Duration.between(dataAnterior, dataAtual).toMillis();
		if (dtMs <= 0) {
			return estoqueAtual;
		}
		Map<TipoRecurso, Long> novo = new EnumMap<>(TipoRecurso.class);
		for (TipoRecurso recurso : TipoRecurso.values()) {
			long atual = estoqueAtual.get(recurso);
			if (atual >= capacidade) {
				novo.put(recurso, atual);
				continue;
			}
			long taxa = taxasHoraPorRecurso.getOrDefault(recurso, 0L);
			long ganho;
			try {
				ganho = ganhoMilesimos(taxa, dtMs);
			}
			catch (ArithmeticException e) {
				// ganho não cabe em long: com certeza excede a capacidade restante
				novo.put(recurso, capacidade);
				continue;
			}
			// comparação em vez de soma evita overflow de atual + ganho
			novo.put(recurso, ganho >= capacidade - atual ? capacidade : atual + ganho);
		}
		return new Estoque(novo);
	}

	/**
	 * Ganho em milésimos: {@code taxa × velocidade × dtMs × 1000 / 3_600_000}. A divisão
	 * ocorre após as multiplicações (mesmo arredondamento de antes).
	 *
	 * @throws ArithmeticException se o produto exceder o intervalo de {@code long}
	 */
	private long ganhoMilesimos(long taxa, long dtMs) {
		long produto = Math.multiplyExact(Math.multiplyExact(Math.multiplyExact(taxa, (long) velocidade), dtMs),
				MILESIMOS_POR_UNIDADE);
		return produto / MILISSEGUNDOS_POR_HORA;
	}

}
