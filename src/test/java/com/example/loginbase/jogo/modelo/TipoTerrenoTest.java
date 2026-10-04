package com.example.loginbase.jogo.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class TipoTerrenoTest {

	@Test
	void temTrezeValoresNaOrdemEsperada() {
		assertThat(TipoTerreno.values()).containsExactly(
				TipoTerreno.FLORESTA, TipoTerreno.BARREIRO, TipoTerreno.PLANTACOES, TipoTerreno.CRIACOES,
				TipoTerreno.ROCHA, TipoTerreno.FERRO, TipoTerreno.CARVAO, TipoTerreno.SALINAS,
				TipoTerreno.ENXOFRE, TipoTerreno.MILITAR, TipoTerreno.INDUSTRIA, TipoTerreno.COMERCIO,
				TipoTerreno.DESENVOLVIMENTO);
	}

	@Test
	void nomesDeExibicaoComAcentos() {
		assertThat(Arrays.stream(TipoTerreno.values()).map(TipoTerreno::getNomeExibicao)).containsExactly(
				"Floresta", "Barreiro", "Plantações", "Criações", "Rocha", "Ferro", "Carvão", "Salinas",
				"Enxofre", "Militar", "Indústria", "Comércio", "Desenvolvimento");
	}
}
