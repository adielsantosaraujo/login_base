package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MasmorraTest {

	@Test
	void criaComValoresValidos() {
		Masmorra m = new Masmorra(1L, 16, 10, 12);
		assertThat(m.getRegiaoIndice()).isEqualTo(16);
		assertThat(m.getNivel()).isEqualTo(10);
		assertThat(m.isAtiva()).isTrue();
		assertThat(m.getTurnosSemAtaque()).isZero();
		assertThat(m.getTurnoUltimoAtaque()).isNull();
		assertThat(new Masmorra(1L, 1, 1, 0).getNivel()).isEqualTo(1);
	}

	@Test
	void rejeitaNivelForaDaFaixa() {
		assertThatThrownBy(() -> new Masmorra(1L, 1, 0, 12)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new Masmorra(1L, 1, 11, 12)).isInstanceOf(IllegalArgumentException.class);
		Masmorra m = new Masmorra(1L, 1, 5, 12);
		assertThatThrownBy(() -> m.setNivel(11)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> m.setNivel(0)).isInstanceOf(IllegalArgumentException.class);
		m.setNivel(6);
		assertThat(m.getNivel()).isEqualTo(6);
	}

	@Test
	void rejeitaRegiaoForaDaFaixa() {
		assertThatThrownBy(() -> new Masmorra(1L, 0, 1, 12)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new Masmorra(1L, 17, 1, 12)).isInstanceOf(IllegalArgumentException.class);
		Masmorra m = new Masmorra(1L, 3, 1, 12);
		assertThatThrownBy(() -> m.setRegiaoIndice(17)).isInstanceOf(IllegalArgumentException.class);
	}

}
