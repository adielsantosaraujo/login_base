package com.example.loginbase.jogo.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.modelo.Jazida;

class GeradorJazidaServiceTest {

	private final GeradorJazidaService gerador = new GeradorJazidaService();

	private Map<Jazida, Integer> contar(Map<String, Jazida> mapa) {
		Map<Jazida, Integer> c = new EnumMap<>(Jazida.class);
		mapa.values().forEach(j -> c.merge(j, 1, Integer::sum));
		return c;
	}

	@Test
	void mesmaSementeEMesmaRegiaoGeramResultadoIgual() {
		assertThat(gerador.gerarJazidasPorRegiao(12345, 1)).isEqualTo(gerador.gerarJazidasPorRegiao(12345, 1));
	}

	@Test
	void sementesDiferentesGeramMapasDiferentes() {
		assertThat(gerador.gerarJazidasPorRegiao(12345, 1)).isNotEqualTo(gerador.gerarJazidasPorRegiao(99999, 1));
	}

	@Test
	void regioesDiferentesGeramMapasDiferentes() {
		assertThat(gerador.gerarJazidasPorRegiao(12345, 1)).isNotEqualTo(gerador.gerarJazidasPorRegiao(12345, 2));
	}

	@Test
	void geraCemLadrilhosDe0a9() {
		Map<String, Jazida> mapa = gerador.gerarJazidasPorRegiao(7, 5);
		assertThat(mapa).hasSize(100).containsKeys("0,0", "9,9", "0,9", "9,0");
	}

	@Test
	void distribuicaoRespeitaPercentuais() {
		for (int r = 1; r <= 16; r++) {
			Map<Jazida, Integer> c = contar(gerador.gerarJazidasPorRegiao(42, r));
			assertThat(c.get(Jazida.FLORESTA)).isEqualTo(25);
			assertThat(c.get(Jazida.ROCHA)).isEqualTo(20);
			assertThat(c.get(Jazida.BARREIRO)).isEqualTo(15);
			assertThat(c.get(Jazida.VEIO_DE_FERRO)).isEqualTo(10);
			assertThat(c.get(Jazida.VEIO_DE_CARVAO)).isEqualTo(10);
			assertThat(c.get(Jazida.SALINA)).isEqualTo(7);
			assertThat(c.get(Jazida.ENXOFRE)).isEqualTo(5);
			assertThat(c.get(Jazida.CAMPO)).isEqualTo(8);
		}
	}

	@Test
	void garantiasMinimasEmVariasSementes() {
		for (long semente = 1; semente <= 100; semente++) {
			for (int r = 1; r <= 16; r++) {
				Map<Jazida, Integer> c = contar(gerador.gerarJazidasPorRegiao(semente, r));
				assertThat(c.get(Jazida.FLORESTA)).isGreaterThanOrEqualTo(10);
				assertThat(c.get(Jazida.ROCHA)).isGreaterThanOrEqualTo(10);
				assertThat(c.get(Jazida.BARREIRO)).isGreaterThanOrEqualTo(8);
			}
		}
	}

	@Test
	void gerarLadrilhosPreencheRegiaoId() {
		var ladrilhos = gerador.gerarLadrilhos(1, 3, 77L);
		assertThat(ladrilhos).hasSize(100).allMatch(l -> l.getRegiaoId() == 77L);
	}

	@Test
	void indiceInvalidoLancaExcecao() {
		assertThatThrownBy(() -> gerador.gerarJazidasPorRegiao(1, 0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> gerador.gerarJazidasPorRegiao(1, 17)).isInstanceOf(IllegalArgumentException.class);
	}

}
