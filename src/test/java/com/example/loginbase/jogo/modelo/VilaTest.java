package com.example.loginbase.jogo.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VilaTest {

	@Test
	void versionIniciaEmZero() {
		Vila vila = new Vila(1L, "Vila", 42L, 1);
		assertThat(vila.getUsuarioId()).isEqualTo(1L);
		assertThat(vila.getVersion()).isZero();
		assertThat(vila.isBemAlimentada()).isFalse();
	}

}
