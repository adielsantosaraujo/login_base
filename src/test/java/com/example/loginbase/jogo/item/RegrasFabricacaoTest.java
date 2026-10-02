package com.example.loginbase.jogo.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.recurso.Recurso;

class RegrasFabricacaoTest {

	private static final Map<Recurso, Integer> BASE = Map.of(Recurso.FERRO, 3, Recurso.TABUA, 1);

	@Test
	void custoMultiplicaPeloNivel() {
		assertThat(RegrasFabricacao.custo(BASE, 2)).isEqualTo(Map.of(Recurso.FERRO, 6, Recurso.TABUA, 2));
	}

	@Test
	void ferroViraAcoAPartirDoNivel6() {
		assertThat(RegrasFabricacao.custo(BASE, 5)).containsEntry(Recurso.FERRO, 15);
		assertThat(RegrasFabricacao.custo(BASE, 6)).isEqualTo(Map.of(Recurso.ACO, 18, Recurso.TABUA, 6));
	}

	@Test
	void aprimoramentoCustaMetadeArredondadaParaCima() {
		assertThat(RegrasFabricacao.custoAprimoramento(BASE, 3))
				.isEqualTo(Map.of(Recurso.FERRO, 5, Recurso.TABUA, 2));
	}

	@Test
	void formulasSimples() {
		assertThat(RegrasFabricacao.pf(4)).isEqualTo(5);
		assertThat(RegrasFabricacao.peMinimo(1)).isZero();
		assertThat(RegrasFabricacao.peMinimo(10)).isEqualTo(18);
		assertThat(RegrasFabricacao.nivelMaximo(NivelConstrucao.N1)).isEqualTo(3);
		assertThat(RegrasFabricacao.nivelMaximo(NivelConstrucao.N2)).isEqualTo(6);
		assertThat(RegrasFabricacao.nivelMaximo(NivelConstrucao.N3)).isEqualTo(10);
		assertThat(RegrasFabricacao.multiplicadorAtributo(1)).isEqualTo(1.0);
		assertThat(RegrasFabricacao.multiplicadorAtributo(6)).isCloseTo(2.0, org.assertj.core.data.Offset.offset(1e-9));
	}

	@Test
	void faixas() {
		assertThat(RegrasFabricacao.faixa(1)).isEqualTo(FaixaBonus.BAIXA);
		assertThat(RegrasFabricacao.faixa(4)).isEqualTo(FaixaBonus.BAIXA);
		assertThat(RegrasFabricacao.faixa(5)).isEqualTo(FaixaBonus.MEDIA);
		assertThat(RegrasFabricacao.faixa(7)).isEqualTo(FaixaBonus.MEDIA);
		assertThat(RegrasFabricacao.faixa(8)).isEqualTo(FaixaBonus.ALTA);
		assertThat(RegrasFabricacao.faixa(10)).isEqualTo(FaixaBonus.ALTA);
	}
}
