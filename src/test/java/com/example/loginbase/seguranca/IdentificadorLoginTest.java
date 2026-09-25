package com.example.loginbase.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.loginbase.seguranca.IdentificadorLogin.Identificador;
import com.example.loginbase.seguranca.IdentificadorLogin.Tipo;

class IdentificadorLoginTest {

	private final IdentificadorLogin identificadorLogin = new IdentificadorLogin();

	@Test
	void entradaNulaResultaEmVazio() {
		assertThat(identificadorLogin.interpretar(null)).isEmpty();
	}

	@Test
	void entradaEmBrancoResultaEmVazio() {
		assertThat(identificadorLogin.interpretar("   ")).isEmpty();
	}

	@Test
	void interpretaEmailComCaixaEEspacosDiferentes() {
		Optional<Identificador> resultado = identificadorLogin.interpretar(" Ana@Exemplo.COM ");

		assertThat(resultado).contains(new Identificador(Tipo.EMAIL, "ana@exemplo.com"));
	}

	@Test
	void interpretaCelularSemMascara() {
		Optional<Identificador> resultado = identificadorLogin.interpretar("11987654321");

		assertThat(resultado).contains(new Identificador(Tipo.CELULAR, "11987654321"));
	}

	@Test
	void interpretaCelularComMascara() {
		Optional<Identificador> resultado = identificadorLogin.interpretar("(11) 98765-4321");

		assertThat(resultado).contains(new Identificador(Tipo.CELULAR, "11987654321"));
	}

	@Test
	void celularComDezDigitosResultaEmVazio() {
		assertThat(identificadorLogin.interpretar("1198765432")).isEmpty();
	}

	@Test
	void celularComDozeDigitosResultaEmVazio() {
		assertThat(identificadorLogin.interpretar("119876543210")).isEmpty();
	}

}
