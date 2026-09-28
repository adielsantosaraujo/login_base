package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.config.AleatorioNomes;
import com.example.loginbase.jogo.suporte.AleatorioSequencia;

/**
 * Testes unitários puros (sem Spring) de {@link GeradorNomes}: carregamento
 * das listas do classpath ({@code jogo/nomes/}), sorteio de nome/sobrenome e
 * falha rápida no construtor quando o arquivo está ausente ou vazio.
 */
class GeradorNomesTest {

	@Test
	void carregaListasDoClasspathSemErroENaoVazias() {
		GeradorNomes gerador = new GeradorNomes(new AleatorioNomes(new AleatorioSequencia(0, 0)));

		NomePessoa nomePessoa = gerador.sortear();

		assertThat(nomePessoa.nome()).isNotBlank();
		assertThat(nomePessoa.sobrenome()).isNotBlank();
	}

	@Test
	void sortearRetornaNomeDaListaDeNomesESobrenomeDaListaDeSobrenomes() {
		// nome_pessoas.json começa com "Ana"; sobrenome_pessoas.json com "Silva".
		GeradorNomes gerador = new GeradorNomes(new AleatorioNomes(new AleatorioSequencia(0, 0)));

		NomePessoa nomePessoa = gerador.sortear();

		assertThat(nomePessoa.nome()).isEqualTo("Ana");
		assertThat(nomePessoa.sobrenome()).isEqualTo("Silva");
	}

	@Test
	void sorteiosSaoDeterministicosComAleatorioSequencia() {
		// segundo nome da lista ("Maria") e segundo sobrenome ("Santos").
		GeradorNomes gerador = new GeradorNomes(new AleatorioNomes(new AleatorioSequencia(0, 0, 1, 1)));

		NomePessoa primeiro = gerador.sortear();
		NomePessoa segundo = gerador.sortear();

		assertThat(primeiro).isEqualTo(new NomePessoa("Ana", "Silva"));
		assertThat(segundo).isEqualTo(new NomePessoa("Maria", "Santos"));
	}

	@Test
	void arquivoAusenteCausaIllegalStateExceptionNoConstrutor() {
		AleatorioNomes aleatorioNomes = new AleatorioNomes(new AleatorioSequencia(0));

		assertThatThrownBy(
				() -> new GeradorNomes(aleatorioNomes, "jogo/nomes/inexistente.json", "jogo/nomes/sobrenome_pessoas.json"))
				.isInstanceOf(IllegalStateException.class);

		assertThatThrownBy(
				() -> new GeradorNomes(aleatorioNomes, "jogo/nomes/nome_pessoas.json", "jogo/nomes/inexistente.json"))
				.isInstanceOf(IllegalStateException.class);
	}

}
