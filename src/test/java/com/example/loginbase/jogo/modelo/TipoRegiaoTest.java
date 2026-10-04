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
				Arguments.of(TipoRegiao.FLORESTA, List.of(TipoTerreno.FLORESTA, TipoTerreno.BARREIRO, TipoTerreno.PLANTACOES)),
				Arguments.of(TipoRegiao.PLANICIE, List.of(TipoTerreno.PLANTACOES, TipoTerreno.CRIACOES, TipoTerreno.FLORESTA)),
				Arguments.of(TipoRegiao.URBANA, List.of(TipoTerreno.INDUSTRIA, TipoTerreno.COMERCIO, TipoTerreno.DESENVOLVIMENTO)),
				Arguments.of(TipoRegiao.LITORAL, List.of(TipoTerreno.SALINAS, TipoTerreno.ENXOFRE, TipoTerreno.MILITAR)),
				Arguments.of(TipoRegiao.MONTANHA, List.of(TipoTerreno.ROCHA, TipoTerreno.FERRO, TipoTerreno.CARVAO)));
	}

	@ParameterizedTest
	@MethodSource("tabela")
	void tipoTemOsTresBonusDaTabelaEmOrdem(TipoRegiao tipo, List<TipoTerreno> esperado) {
		assertThat(tipo.terrenos()).containsExactlyElementsOf(esperado);
		assertThat(tipo.terrenos()).doesNotHaveDuplicates().hasSize(3);
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
		Map<TipoTerreno, Integer> contagem = new EnumMap<>(TipoTerreno.class);
		for (TipoRegiao tipo : TipoRegiao.atuais()) {
			for (TipoTerreno b : tipo.terrenos()) {
				contagem.merge(b, 1, Integer::sum);
			}
		}
		assertThat(TipoTerreno.values()).hasSize(13);
		assertThat(contagem.keySet()).isEqualTo(EnumSet.allOf(TipoTerreno.class));
		assertThat(contagem.values()).allMatch(n -> n >= 1 && n <= 2);
	}

	@Test
	void ordemEnomesDosBonus() {
		assertThat(Arrays.stream(TipoTerreno.values()).map(TipoTerreno::getNomeExibicao)).containsExactly(
				"Floresta", "Barreiro", "Plantações", "Criações", "Rocha", "Ferro", "Carvão", "Salinas",
				"Enxofre", "Militar", "Indústria", "Comércio", "Desenvolvimento");
	}
}
