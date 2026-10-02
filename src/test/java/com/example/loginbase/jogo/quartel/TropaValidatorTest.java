package com.example.loginbase.jogo.quartel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;

class TropaValidatorTest {

	private final TropaValidator v = new TropaValidator();

	private Cidadao cidadao(int anos) {
		return new Cidadao(1L, 1L, "C", Sexo.M, anos * 12);
	}

	private void erro(Runnable r, String msg) {
		assertThatThrownBy(r::run).isInstanceOfSatisfying(JogoException.class, e -> {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).isEqualTo(msg);
		});
	}

	@Test
	void elegivelComTudoOk() {
		assertThat(v.motivoInelegivel(cidadao(20), 1, true)).isEmpty();
	}

	@Test
	void semArma() {
		erro(() -> v.validarMembro(cidadao(20), 1, false), "Cidadão não tem arma equipada");
	}

	@Test
	void peInsuficiente() {
		erro(() -> v.validarMembro(cidadao(20), 0, true), "PE Guerreiro insuficiente");
	}

	@Test
	void limitesDeIdade() {
		assertThat(v.motivoInelegivel(cidadao(15), 1, true)).isPresent();
		assertThat(v.motivoInelegivel(cidadao(16), 1, true)).isEmpty();
		assertThat(v.motivoInelegivel(cidadao(54), 1, true)).isEmpty();
		assertThat(v.motivoInelegivel(cidadao(55), 1, true)).isPresent();
	}

	@Test
	void jaEmTropaAlocadoMortoEFerido() {
		Cidadao emTropa = cidadao(20);
		emTropa.setTropaId(5L);
		erro(() -> v.validarMembro(emTropa, 1, true), "Cidadão já está em tropa");
		Cidadao alocado = cidadao(20);
		alocado.setConstrucaoId(9L);
		erro(() -> v.validarMembro(alocado, 1, true), "Cidadão alocado em prédio");
		Cidadao morto = cidadao(20);
		morto.setVivo(false);
		erro(() -> v.validarMembro(morto, 1, true), "Cidadão morto");
		Cidadao ferido = cidadao(20);
		ferido.setEstado(EstadoCidadao.FERIDO);
		erro(() -> v.validarMembro(ferido, 1, true), "Cidadão ferido");
	}

	@Test
	void quartelPrecisaEstarAtivoEComInstrutor() {
		Construcao q = new Construcao();
		q.setEstado(EstadoConstrucao.ATIVA);
		erro(() -> v.validarQuartel(q, 0), "Quartel sem instrutor");
		assertThatCode(() -> v.validarQuartel(q, 1)).doesNotThrowAnyException();
		q.setEstado(EstadoConstrucao.EM_OBRA);
		erro(() -> v.validarQuartel(q, 1), "Quartel não está ativo");
	}

	@Test
	void capacidadeTotalEMaxTropas() {
		assertThatCode(() -> v.validarCapacidade(NivelConstrucao.N1, 3, 2)).doesNotThrowAnyException();
		erro(() -> v.validarCapacidade(NivelConstrucao.N1, 5, 1), "Capacidade do quartel excedida");
		assertThatCode(() -> v.validarCapacidade(NivelConstrucao.N2, 5, 3)).doesNotThrowAnyException();
		erro(() -> v.validarCapacidade(NivelConstrucao.N3, 9, 2), "Capacidade do quartel excedida");
		assertThatCode(() -> v.validarLimiteTropas(NivelConstrucao.N3, 3)).doesNotThrowAnyException();
		erro(() -> v.validarLimiteTropas(NivelConstrucao.N3, 4), "Limite de tropas do quartel atingido");
		erro(() -> v.validarLimiteTropas(NivelConstrucao.N1, 1), "Limite de tropas do quartel atingido");
	}
}
