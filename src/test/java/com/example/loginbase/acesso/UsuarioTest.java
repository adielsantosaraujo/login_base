package com.example.loginbase.acesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UsuarioTest {

	@Test
	void normalizaEmailRemovendoEspacosEConvertendoParaMinusculas() {
		Usuario usuario = new Usuario();

		usuario.setEmail(" Ana@Exemplo.COM ");

		assertThat(usuario.getEmail()).isEqualTo("ana@exemplo.com");
	}

	@Test
	void normalizaCelularComMascaraParaSomenteDigitos() {
		Usuario usuario = new Usuario();

		usuario.setCelular("(11) 98765-4321");

		assertThat(usuario.getCelular()).isEqualTo("11987654321");
	}

	@Test
	void rejeitaCelularComDezDigitos() {
		Usuario usuario = new Usuario();

		assertThatThrownBy(() -> usuario.setCelular("1198765432"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Celular deve ter 11 dígitos (DDD + número)");
	}

	@Test
	void rejeitaCelularComDozeDigitos() {
		Usuario usuario = new Usuario();

		assertThatThrownBy(() -> usuario.setCelular("119876543210"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Celular deve ter 11 dígitos (DDD + número)");
	}

	@Test
	void rejeitaCelularComLetraQueResultaEmMenosDeOnzeDigitos() {
		Usuario usuario = new Usuario();

		assertThatThrownBy(() -> usuario.setCelular("1198765432a"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Celular deve ter 11 dígitos (DDD + número)");
	}

	@Test
	void celularNuloFicaNulo() {
		Usuario usuario = new Usuario();

		usuario.setCelular(null);

		assertThat(usuario.getCelular()).isNull();
	}

	@Test
	void celularEmBrancoFicaNulo() {
		Usuario usuario = new Usuario();

		usuario.setCelular("   ");

		assertThat(usuario.getCelular()).isNull();
	}

	@Test
	void emailNuloFicaNulo() {
		Usuario usuario = new Usuario();

		usuario.setEmail(null);

		assertThat(usuario.getEmail()).isNull();
	}

}
