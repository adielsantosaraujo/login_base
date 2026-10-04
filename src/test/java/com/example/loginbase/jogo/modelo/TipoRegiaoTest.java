package com.example.loginbase.jogo.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TipoRegiaoTest {

	static Stream<Arguments> tabela() {
		return Stream.of(
				Arguments.of(TipoRegiao.FLORESTA, List.of(BonusRegiao.FLORESTA, BonusRegiao.BARREIRO, BonusRegiao.PLANTACOES)),
				Arguments.of(TipoRegiao.PLANICIE, List.of(BonusRegiao.PLANTACOES, BonusRegiao.CRIACOES, BonusRegiao.FLORESTA)),
				Arguments.of(TipoRegiao.URBANA, List.of(BonusRegiao.INDUSTRIA, BonusRegiao.COMERCIO, BonusRegiao.DESENVOLVIMENTO)),
				Arguments.of(TipoRegiao.LITORAL, List.of(BonusRegiao.SALINAS, BonusRegiao.ENXOFRE, BonusRegiao.MILITAR)),
				Arguments.of(TipoRegiao.MONTANHA, List.of(BonusRegiao.ROCHA, BonusRegiao.FERRO, BonusRegiao.CARVAO)));
	}

	@ParameterizedTest
	@MethodSource("tabela")
	void tipoTemOsTresBonusDaTabelaEmOrdem(TipoRegiao tipo, List<BonusRegiao> esperado) {
		assertThat(tipo.bonus()).containsExactlyElementsOf(esperado);
		assertThat(tipo.bonus()).doesNotHaveDuplicates().hasSize(3);
	}

	@Test
	void atuaisTemCincoTiposEValuesCinco() {
		assertThat(TipoRegiao.atuais()).hasSize(5);
		assertThat(TipoRegiao.values()).hasSize(5);
	}

	@Test
	void nomesDeExibicao() {
		assertThat(TipoRegiao.atuais()).extracting(TipoRegiao::getNomeExibicao)
				.containsExactly("Floresta", "Planície", "Urbana", "Litoral", "Montanha");
	}

	@Test
	void treze_bonus_cada_um_em_um_ou_dois_tipos() {
		Map<BonusRegiao, Integer> contagem = new EnumMap<>(BonusRegiao.class);
		for (TipoRegiao tipo : TipoRegiao.atuais()) {
			for (BonusRegiao b : tipo.bonus()) {
				contagem.merge(b, 1, Integer::sum);
			}
		}
		assertThat(BonusRegiao.values()).hasSize(13);
		assertThat(contagem.keySet()).isEqualTo(EnumSet.allOf(BonusRegiao.class));
		assertThat(contagem.values()).allMatch(n -> n >= 1 && n <= 2);
	}

	@Test
	void ordemEnomesDosBonus() {
		assertThat(Arrays.stream(BonusRegiao.values()).map(BonusRegiao::getNomeExibicao)).containsExactly(
				"Floresta", "Barreiro", "Plantações", "Criações", "Rocha", "Ferro", "Carvão", "Salinas",
				"Enxofre", "Militar", "Indústria", "Comércio", "Desenvolvimento");
	}

	@Test
	void faixasInclusivas() {
		assertThat(FaixaBonusRegiao.de(1).getMin()).isEqualTo(35);
		assertThat(FaixaBonusRegiao.de(1).getMax()).isEqualTo(50);
		assertThat(FaixaBonusRegiao.de(2).getMin()).isEqualTo(16);
		assertThat(FaixaBonusRegiao.de(2).getMax()).isEqualTo(34);
		assertThat(FaixaBonusRegiao.de(3).getMin()).isEqualTo(5);
		assertThat(FaixaBonusRegiao.de(3).getMax()).isEqualTo(15);
	}

	@Test
	void posicaoForaDe1a3Falha() {
		assertThatThrownBy(() -> FaixaBonusRegiao.de(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> FaixaBonusRegiao.de(4)).isInstanceOf(IllegalArgumentException.class);
	}
}
