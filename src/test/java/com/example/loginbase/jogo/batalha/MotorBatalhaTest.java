package com.example.loginbase.jogo.batalha;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.item.catalogo.Alcance;

class MotorBatalhaTest {

	private final MotorBatalha motor = new MotorBatalha();

	private static Combatente c(long id, LadoCombate lado, LinhaCombate linha, Alcance alc, int pv, double atq,
			double def, int ini, double crit, int vel) {
		return new Combatente(id, lado, (lado == LadoCombate.TROPA ? "G" : "I") + id, linha, pv, atq, def, ini, crit,
				vel, alc, false);
	}

	private static Combatente tropa(long id, LinhaCombate linha, Alcance alc, int pv, double atq) {
		return c(id, LadoCombate.TROPA, linha, alc, pv, atq, 10, 10, 0, 0);
	}

	private static Combatente inimigo(long id, LinhaCombate linha, Alcance alc, int pv, double atq) {
		return c(id, LadoCombate.INIMIGO, linha, alc, pv, atq, 10, 10, 0, 0);
	}

	@Test
	void determinismo() {
		var t = List.of(tropa(1, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 80, 20),
				tropa(2, LinhaCombate.RETAGUARDA, Alcance.DISTANCIA, 60, 18));
		var i = List.of(inimigo(0, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 90, 22),
				inimigo(1, LinhaCombate.RETAGUARDA, Alcance.DISTANCIA, 50, 15));
		assertThat(motor.resolver(t, i, 42L)).isEqualTo(motor.resolver(t, i, 42L));
	}

	@Test
	void goblinCaiEmDuasRodadas() {
		Combatente g = c(123, LadoCombate.TROPA, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 79, 29, 15, 18, 0,
				0);
		Combatente goblin = TipoInimigo.GOBLIN.criar(0, 1);
		ResultadoBatalha r = motor.resolver(List.of(g), List.of(goblin), 7L);
		assertThat(r.resultado()).isEqualTo(ResultadoCombate.VITORIA);
		assertThat(r.rodadas()).isEqualTo(2);
		assertThat(r.log().stream().filter(a -> a.atacanteLado() == LadoCombate.TROPA)).hasSize(2);
		assertThat(r.pvFinal().stream().filter(p -> p.lado() == LadoCombate.INIMIGO).findFirst().get().pv())
				.isZero();
		assertThat(r.abatidosTropa()).isEmpty();
	}

	@Test
	void limiteDeTrintaRodadasEhDerrota() {
		var t = List.of(c(1, LadoCombate.TROPA, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 1000, 1, 1000, 5,
				0, 0));
		var i = List.of(c(0, LadoCombate.INIMIGO, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 1000, 1, 1000, 5,
				0, 0));
		ResultadoBatalha r = motor.resolver(t, i, 1L);
		assertThat(r.resultado()).isEqualTo(ResultadoCombate.DERROTA);
		assertThat(r.rodadas()).isEqualTo(30);
		assertThat(r.log()).hasSize(60);
	}

	@Test
	void espadaNaRetaguardaNaoAtaca() {
		var t = List.of(tropa(1, LinhaCombate.RETAGUARDA, Alcance.CORPO_A_CORPO_FRENTE, 1000, 50));
		var i = List.of(inimigo(0, LinhaCombate.RETAGUARDA, Alcance.DISTANCIA, 1000, 1));
		ResultadoBatalha r = motor.resolver(t, i, 3L);
		assertThat(r.log()).noneMatch(a -> a.atacanteLado() == LadoCombate.TROPA);
		assertThat(r.resultado()).isEqualTo(ResultadoCombate.DERROTA);
	}

	@Test
	void lancaNaRetaguardaAtingeSoFrenteInimiga() {
		Combatente lanca = tropa(1, LinhaCombate.RETAGUARDA, Alcance.CORPO_A_CORPO_FLEX, 1000, 5);
		Combatente frente = inimigo(0, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 1000, 1);
		Combatente reta = inimigo(1, LinhaCombate.RETAGUARDA, Alcance.DISTANCIA, 10, 1);
		assertThat(ValidadorAlvo.alvosPermitidos(lanca, List.of(frente, reta))).containsExactly(frente);
		ResultadoBatalha r = motor.resolver(List.of(lanca), List.of(frente, reta), 5L);
		assertThat(r.log()).filteredOn(a -> a.atacanteLado() == LadoCombate.TROPA).isNotEmpty()
				.allMatch(a -> a.alvoId() == 0);
	}

	@Test
	void arcoEBestaAtingemRetaguarda() {
		Combatente frente = inimigo(0, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 100, 1);
		Combatente reta = inimigo(1, LinhaCombate.RETAGUARDA, Alcance.DISTANCIA, 10, 1);
		Combatente arco = tropa(1, LinhaCombate.RETAGUARDA, Alcance.DISTANCIA, 100, 5);
		assertThat(ValidadorAlvo.alvosPermitidos(arco, List.of(frente, reta))).containsExactly(frente, reta);
		// tropa mira o de menor PV (retaguarda, 10 PV)
		ResultadoBatalha r = motor.resolver(List.of(arco), List.of(frente, reta), 9L);
		assertThat(r.log().get(0).atacanteLado() == LadoCombate.TROPA ? r.log().get(0).alvoId() : 1L).isEqualTo(1L);
	}

	@Test
	void frenteInimigaVaziaLiberaRetaguardaParaCorpoACorpo() {
		Combatente espada = tropa(1, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 100, 5);
		Combatente reta = inimigo(1, LinhaCombate.RETAGUARDA, Alcance.DISTANCIA, 10, 1);
		assertThat(ValidadorAlvo.alvosPermitidos(espada, List.of(reta))).containsExactly(reta);
	}

	@Test
	void ordemDeIniciativaEDesempate() {
		Combatente a = c(5, LadoCombate.TROPA, LinhaCombate.FRENTE, Alcance.DISTANCIA, 10, 1, 1, 0, 0, 3);
		Combatente b = c(2, LadoCombate.TROPA, LinhaCombate.FRENTE, Alcance.DISTANCIA, 10, 1, 1, 0, 0, 3);
		Combatente veloz = c(9, LadoCombate.TROPA, LinhaCombate.FRENTE, Alcance.DISTANCIA, 10, 1, 1, 0, 0, 8);
		Combatente inim = c(2, LadoCombate.INIMIGO, LinhaCombate.FRENTE, Alcance.DISTANCIA, 10, 1, 1, 0, 0, 3);
		Combatente topo = c(7, LadoCombate.INIMIGO, LinhaCombate.FRENTE, Alcance.DISTANCIA, 10, 1, 1, 0, 0, 0);
		var ordem = CalculadorIniciativa.ordenarPorTotais(List.of(new CalculadorIniciativa.Entrada(a, 10),
				new CalculadorIniciativa.Entrada(inim, 10), new CalculadorIniciativa.Entrada(b, 10),
				new CalculadorIniciativa.Entrada(veloz, 10), new CalculadorIniciativa.Entrada(topo, 15)));
		// maior total; depois VEL desc; depois id asc; depois TROPA antes de INIMIGO
		assertThat(ordem).containsExactly(topo, veloz, b, inim, a);
	}

	@Test
	void iniciativaSomaD6() {
		Combatente x = c(1, LadoCombate.TROPA, LinhaCombate.FRENTE, Alcance.DISTANCIA, 10, 1, 1, 18, 0, 0);
		for (long s = 0; s < 20; s++) {
			int t = CalculadorIniciativa.rolar(x, new java.util.Random(s));
			assertThat(t).isBetween(19, 24);
		}
	}

	@Test
	void criticoMultiplicaPorUmVirgulaCinco() {
		Combatente semCrit = c(1, LadoCombate.TROPA, LinhaCombate.FRENTE, Alcance.DISTANCIA, 1000, 30, 10, 100, 0, 0);
		Combatente comCrit = c(1, LadoCombate.TROPA, LinhaCombate.FRENTE, Alcance.DISTANCIA, 1000, 30, 10, 100, 100,
				0);
		Combatente alvo = c(0, LadoCombate.INIMIGO, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 1000, 1, 10, 0,
				0, 0);
		AcaoBatalha n = motor.resolver(List.of(semCrit), List.of(alvo), 11L).log().get(0);
		AcaoBatalha k = motor.resolver(List.of(comCrit), List.of(alvo), 11L).log().get(0);
		assertThat(n.critico()).isFalse();
		assertThat(k.critico()).isTrue();
		assertThat(k.dano()).isEqualTo((int) Math.round(n.dano() * 1.5));
		assertThat(k.pvAntes()).isEqualTo(1000);
		assertThat(k.pvDepois()).isEqualTo(1000 - k.dano());
	}

	@Test
	void abatidoNaoAgeMais() {
		var t = List.of(tropa(1, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 1, 1));
		var i = List.of(inimigo(0, LinhaCombate.FRENTE, Alcance.CORPO_A_CORPO_FRENTE, 500, 100));
		ResultadoBatalha r = motor.resolver(t, i, 2L);
		assertThat(r.resultado()).isEqualTo(ResultadoCombate.DERROTA);
		assertThat(r.abatidosTropa()).containsExactly(1L);
		assertThat(r.log().get(r.log().size() - 1).abatido()).isTrue();
		assertThat(r.log().stream().filter(a -> a.atacanteId() == 1 && a.atacanteLado() == LadoCombate.TROPA).count())
				.isLessThanOrEqualTo(1);
	}
}
