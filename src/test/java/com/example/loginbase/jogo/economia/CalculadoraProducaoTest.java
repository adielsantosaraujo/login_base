package com.example.loginbase.jogo.economia;

import static com.example.loginbase.jogo.catalogo.TipoRecurso.COMIDA;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.FERRO;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.MADEIRA;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.PEDRA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.CurvaNiveis;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoRecurso;

/**
 * Testes unitários puros (sem Spring) de {@link CalculadoraProducao} e
 * {@link Estoque}: validam fórmulas de taxa/hora, capacidade, milésimos,
 * velocidade e esgotamento de acordo com o design (Recursos, Prédios,
 * Cultivos, Velocidade e Cálculo Preguiçoso do Tempo).
 */
class CalculadoraProducaoTest {

	private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

	@Test
	void construtorRejeitaVelocidadeMenorQueUm() {
		assertThatThrownBy(() -> new CalculadoraProducao(0, CurvaNiveis.PADRAO)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void taxaHoraSerrariaCresceComNivel() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);

		Map<TipoRecurso, Long> n1 = calc.taxaHoraPorRecurso(Map.of(TipoPredio.SERRARIA, 1), List.of());
		Map<TipoRecurso, Long> n2 = calc.taxaHoraPorRecurso(Map.of(TipoPredio.SERRARIA, 2), List.of());

		assertThat(n1.get(MADEIRA)).isEqualTo(30L);
		assertThat(n2.get(MADEIRA)).isEqualTo(60L);
	}

	@Test
	void taxaHoraPedreiraEMinaFerroSeguemFormulasDoDesign() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);

		Map<TipoRecurso, Long> taxas = calc.taxaHoraPorRecurso(
				Map.of(TipoPredio.PEDREIRA, 3, TipoPredio.MINA_FERRO, 2), List.of());

		assertThat(taxas.get(PEDRA)).isEqualTo(60L); // 20 × 3
		assertThat(taxas.get(FERRO)).isEqualTo(20L); // 10 × 2
	}

	@Test
	void taxaHoraComidaSomaTodosOsCanteiros() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);

		Map<TipoRecurso, Long> umCanteiro = calc.taxaHoraPorRecurso(Map.of(), List.of(Cultivo.TRIGO));
		Map<TipoRecurso, Long> tresCanteiros = calc.taxaHoraPorRecurso(Map.of(),
				List.of(Cultivo.TRIGO, Cultivo.MILHO, Cultivo.BATATA));

		assertThat(umCanteiro.get(COMIDA)).isEqualTo(20L);
		assertThat(tresCanteiros.get(COMIDA)).isEqualTo(20L + 30L + 45L);
	}

	@Test
	void taxaHoraPredioNivelZeroOuAusenteNaoProduz() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);

		Map<TipoRecurso, Long> taxas = calc.taxaHoraPorRecurso(Map.of(TipoPredio.SERRARIA, 0), List.of());

		assertThat(taxas.get(MADEIRA)).isZero();
		assertThat(taxas.get(PEDRA)).isZero();
		assertThat(taxas.get(FERRO)).isZero();
	}

	@Test
	void construtorRejeitaCurvaNula() {
		assertThatThrownBy(() -> new CalculadoraProducao(1, null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void capacidadeMaximaSeguerFormula500Vezes2ElevadoNMenos1EmMilesimos() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);

		assertThat(calc.capacidadeMaxima(1)).isEqualTo(500_000L);
		assertThat(calc.capacidadeMaxima(2)).isEqualTo(1_000_000L);
		assertThat(calc.capacidadeMaxima(5)).isEqualTo(8_000_000L);
		assertThat(calc.capacidadeMaxima(6)).isEqualTo(10_516_000L);
		assertThat(calc.capacidadeMaxima(100)).isEqualTo(715_542_000L);
	}

	@Test
	void producaoDeUmaHoraComSerrariaNivel1SomaTrintaUnidadesEmMilesimos() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);
		Map<TipoRecurso, Long> taxas = calc.taxaHoraPorRecurso(Map.of(TipoPredio.SERRARIA, 1), List.of());
		Estoque inicial = Estoque.vazio();

		Estoque resultado = calc.produzirAte(inicial, taxas, calc.capacidadeMaxima(1), T0, T0.plus(Duration.ofHours(1)));

		assertThat(resultado.get(MADEIRA)).isEqualTo(30_000L);
	}

	@Test
	void velocidadeMultiplicaTaxaEfetivaDeProducao() {
		// taxa 30/h (serraria N1), velocidade 2, 1 hora real → efetivo 2h de produção base = +60 unidades
		CalculadoraProducao calc = new CalculadoraProducao(2, CurvaNiveis.PADRAO);
		Map<TipoRecurso, Long> taxas = calc.taxaHoraPorRecurso(Map.of(TipoPredio.SERRARIA, 1), List.of());
		Estoque inicial = Estoque.vazio();

		Estoque resultado = calc.produzirAte(inicial, taxas, calc.capacidadeMaxima(5), T0, T0.plus(Duration.ofHours(1)));

		assertThat(resultado.get(MADEIRA)).isEqualTo(60_000L);
	}

	@Test
	void esgotamentoDeCapacidadeNaoUltrapassaOLimite() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);
		// taxa 100/h por 1 hora → ganho de 100 unidades, mas estoque parte de 450 com capacidade 500.
		Map<TipoRecurso, Long> taxas = Map.of(COMIDA, 100L);
		Estoque inicial = new Estoque(Map.of(COMIDA, 450_000L));

		Estoque resultado = calc.produzirAte(inicial, taxas, 500_000L, T0, T0.plus(Duration.ofHours(1)));

		assertThat(resultado.get(COMIDA)).isEqualTo(500_000L);
	}

	@Test
	void estoqueJaNoLimiteOuAcimaPermaneceInalterado() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);
		Map<TipoRecurso, Long> taxas = Map.of(COMIDA, 100L);
		Estoque noLimite = new Estoque(Map.of(COMIDA, 500_000L));

		Estoque resultado = calc.produzirAte(noLimite, taxas, 500_000L, T0, T0.plus(Duration.ofHours(1)));

		assertThat(resultado.get(COMIDA)).isEqualTo(500_000L);
	}

	@Test
	void producaoQueEstouraLongSaturaNaCapacidadeSemValorNegativo() {
		CalculadoraProducao calc = new CalculadoraProducao(1_000_000, CurvaNiveis.PADRAO);
		long capacidade = calc.capacidadeMaxima(100);
		Map<TipoRecurso, Long> taxas = Map.of(MADEIRA, 3000L);

		Estoque resultado = calc.produzirAte(Estoque.vazio(), taxas, capacidade, T0, T0.plus(Duration.ofDays(36_500)));

		assertThat(resultado.get(MADEIRA)).isEqualTo(capacidade);
		assertThat(resultado.get(COMIDA)).isZero();
	}

	@Test
	void somaAtualMaisGanhoQueEstouraLongSaturaNaCapacidade() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);
		// ganho de 1_000_000 milésimos cabe em long, mas atual + ganho estouraria
		Estoque inicial = new Estoque(Map.of(COMIDA, Long.MAX_VALUE - 10));

		Estoque resultado = calc.produzirAte(inicial, Map.of(COMIDA, 1000L), Long.MAX_VALUE, T0,
				T0.plus(Duration.ofHours(1)));

		assertThat(resultado.get(COMIDA)).isEqualTo(Long.MAX_VALUE);
	}

	@Test
	void semTempoDecorridoNaoAlteraOEstoque() {
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);
		Map<TipoRecurso, Long> taxas = Map.of(MADEIRA, 30L);
		Estoque inicial = Estoque.vazio();

		Estoque resultado = calc.produzirAte(inicial, taxas, 500_000L, T0, T0);

		assertThat(resultado).isEqualTo(inicial);
	}

	@Test
	void trechosComTaxasDiferentesAntesEDepoisDeUpgradeSaoAcumulados() {
		// Serraria sobe de N1 para N2 no meio do intervalo: 1h com taxa 30/h, depois 1h com taxa 60/h.
		CalculadoraProducao calc = new CalculadoraProducao(1, CurvaNiveis.PADRAO);
		Instant t1 = T0.plus(Duration.ofHours(1));
		Instant t2 = t1.plus(Duration.ofHours(1));

		Map<TipoRecurso, Long> taxaAntes = calc.taxaHoraPorRecurso(Map.of(TipoPredio.SERRARIA, 1), List.of());
		Map<TipoRecurso, Long> taxaDepois = calc.taxaHoraPorRecurso(Map.of(TipoPredio.SERRARIA, 2), List.of());

		Estoque apoisPrimeiroTrecho = calc.produzirAte(Estoque.vazio(), taxaAntes, calc.capacidadeMaxima(5), T0, t1);
		Estoque resultadoFinal = calc.produzirAte(apoisPrimeiroTrecho, taxaDepois, calc.capacidadeMaxima(5), t1, t2);

		assertThat(resultadoFinal.get(MADEIRA)).isEqualTo(30_000L + 60_000L);
	}

	@Test
	void estoqueGetPadraoParaRecursoAusenteEZero() {
		assertThat(Estoque.vazio().get(FERRO)).isZero();
	}

	@Test
	void estoqueTemValidaSaldoDisponivel() {
		Estoque estoque = new Estoque(Map.of(PEDRA, 100_000L));

		assertThat(estoque.tem(PEDRA, 100_000L)).isTrue();
		assertThat(estoque.tem(PEDRA, 100_001L)).isFalse();
	}

	@Test
	void estoqueAdicionarSomaAoRecurso() {
		Estoque estoque = new Estoque(Map.of(PEDRA, 100_000L));

		Estoque resultado = estoque.adicionar(PEDRA, 50_000L);

		assertThat(resultado.get(PEDRA)).isEqualTo(150_000L);
		assertThat(estoque.get(PEDRA)).isEqualTo(100_000L); // imutável: original não muda
	}

	@Test
	void estoqueSubtrairComSaldoSuficienteReduzORecurso() {
		Estoque estoque = new Estoque(Map.of(FERRO, 100_000L));

		Estoque resultado = estoque.subtrair(FERRO, 40_000L);

		assertThat(resultado.get(FERRO)).isEqualTo(60_000L);
	}

	@Test
	void estoqueSubtrairSemSaldoLancaRecursosInsuficientes() {
		Estoque estoque = new Estoque(Map.of(FERRO, 10_000L));

		assertThatThrownBy(() -> estoque.subtrair(FERRO, 20_000L))
				.isInstanceOf(RegraJogoException.class)
				.satisfies(ex -> assertThat(((RegraJogoException) ex).getCodigo())
						.isEqualTo(CodigoErro.RECURSOS_INSUFICIENTES));
	}

	@Test
	void estoqueComValorNegativoNaConstrucaoLancaExcecao() {
		assertThatThrownBy(() -> new Estoque(Map.of(FERRO, -1L))).isInstanceOf(IllegalArgumentException.class);
	}

}
