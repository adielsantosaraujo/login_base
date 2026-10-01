package com.example.loginbase.jogo.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GradeRegioesTest {

	@Test
	void linhaEColuna() {
		assertThat(GradeRegioes.linha(1)).isEqualTo(0);
		assertThat(GradeRegioes.coluna(1)).isEqualTo(0);
		assertThat(GradeRegioes.linha(7)).isEqualTo(1);
		assertThat(GradeRegioes.coluna(7)).isEqualTo(2);
		assertThat(GradeRegioes.linha(16)).isEqualTo(3);
		assertThat(GradeRegioes.coluna(16)).isEqualTo(3);
	}

	@Test
	void vizinhos() {
		assertThat(GradeRegioes.vizinhos(1)).containsExactly(2, 5);
		assertThat(GradeRegioes.vizinhos(6)).containsExactly(2, 5, 7, 10);
		assertThat(GradeRegioes.vizinhos(8)).containsExactly(4, 7, 12);
		assertThat(GradeRegioes.vizinhos(16)).containsExactly(12, 15);
	}

	@Test
	void adjacente() {
		assertThat(GradeRegioes.adjacente(6, 7)).isTrue();
		assertThat(GradeRegioes.adjacente(7, 6)).isTrue();
		assertThat(GradeRegioes.adjacente(6, 10)).isTrue();
		assertThat(GradeRegioes.adjacente(4, 5)).isFalse(); // borda da grade, sem "dar a volta"
		assertThat(GradeRegioes.adjacente(1, 6)).isFalse(); // diagonal
		assertThat(GradeRegioes.adjacente(6, 6)).isFalse();
		assertThat(GradeRegioes.adjacente(1, 16)).isFalse();
	}

	@Test
	void indiceInvalido() {
		assertThatThrownBy(() -> GradeRegioes.linha(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> GradeRegioes.adjacente(1, 17)).isInstanceOf(IllegalArgumentException.class);
	}

}
