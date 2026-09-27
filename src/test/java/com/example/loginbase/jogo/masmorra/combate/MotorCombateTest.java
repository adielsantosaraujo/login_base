package com.example.loginbase.jogo.masmorra.combate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.MapaMasmorra;

/**
 * Testes unitários puros (sem Spring) do motor de combate tático: movimento
 * (com BFS e obstáculos), ataque/dano, defesa, IA determinística dos
 * inimigos, controle de turno e fim de batalha (vitória/derrota).
 */
class MotorCombateTest {

	private static final MapaMasmorra MAPA_SEM_OBSTACULOS = mapa(8, 8, List.of());

	@Test
	void movimentoDentroDoAlcanceEAceito() {
		Combatente soldado = jogador("J1", 2, 7, 30, 8, 3, 1, 3);
		EstadoBatalha estadoOriginal = novoEstado(List.of(soldado), 1, MAPA_SEM_OBSTACULOS);

		EstadoBatalha novoEstado = MotorCombate.processar(estadoOriginal, AcaoCombate.mover("J1", 2, 4, 1));

		assertThat(novoEstado).isNotSameAs(estadoOriginal);
		assertThat(estadoOriginal.buscar("J1").orElseThrow().posicao()).isEqualTo(new Posicao(2, 7));
		Combatente movido = novoEstado.buscar("J1").orElseThrow();
		assertThat(movido.posicao()).isEqualTo(new Posicao(2, 4));
		assertThat(movido.moveu()).isTrue();
		assertThat(novoEstado.log()).contains("J1 moveu para (2,4).");
	}

	@Test
	void movimentoAcimaDoLimiteRejeitado() {
		Combatente soldado = jogador("J1", 2, 7, 30, 8, 3, 1, 3);
		EstadoBatalha estado = novoEstado(List.of(soldado), 1, MAPA_SEM_OBSTACULOS);

		assertThatThrownBy(() -> MotorCombate.processar(estado, AcaoCombate.mover("J1", 2, 1, 1)))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.ACAO_INVALIDA);
	}

	@Test
	void bfsContornaObstaculoQuandoMovimentoSuficiente() {
		// Grade 3x2, obstáculo em (1,0) bloqueia o caminho direto entre
		// (0,0) e (2,0); contorno via (0,1)-(1,1)-(2,1)-(2,0) tem 4 passos.
		MapaMasmorra mapa = mapa(3, 2, List.of(new MapaMasmorra.Posicao(1, 0)));
		Combatente soldado = jogador("J1", 0, 0, 30, 8, 3, 1, 4);
		EstadoBatalha estado = novoEstado(List.of(soldado), 1, mapa);

		EstadoBatalha novoEstado = MotorCombate.processar(estado, AcaoCombate.mover("J1", 2, 0, 1));

		assertThat(novoEstado.buscar("J1").orElseThrow().posicao()).isEqualTo(new Posicao(2, 0));
	}

	@Test
	void bfsRejeitaQuandoContornoExcedeMovimento() {
		MapaMasmorra mapa = mapa(3, 2, List.of(new MapaMasmorra.Posicao(1, 0)));
		Combatente soldado = jogador("J1", 0, 0, 30, 8, 3, 1, 3);
		EstadoBatalha estado = novoEstado(List.of(soldado), 1, mapa);

		assertThatThrownBy(() -> MotorCombate.processar(estado, AcaoCombate.mover("J1", 2, 0, 1)))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.ACAO_INVALIDA);
	}

	@Test
	void ataqueComDanoMinimoDeUm() {
		Combatente arqueiro = jogador("J1", 0, 0, 20, 4, 1, 3, 2);
		Combatente orc = inimigo("I1", 0, 1, 30, 9, 5, 1, 2);
		EstadoBatalha estado = novoEstado(List.of(arqueiro, orc), 1, MAPA_SEM_OBSTACULOS);

		EstadoBatalha novoEstado = MotorCombate.processar(estado, AcaoCombate.atacar("J1", "I1", 1));

		assertThat(novoEstado.buscar("I1").orElseThrow().hp()).isEqualTo(29);
		assertThat(novoEstado.log()).contains("J1 atacou I1 causando 1 dano.");
	}

	@Test
	void alvoForaDoAlcanceRejeitado() {
		Combatente soldado = jogador("J1", 0, 0, 30, 8, 3, 1, 3);
		Combatente goblin = inimigo("I1", 2, 0, 15, 6, 1, 1, 3);
		EstadoBatalha estado = novoEstado(List.of(soldado, goblin), 1, MAPA_SEM_OBSTACULOS);

		assertThatThrownBy(() -> MotorCombate.processar(estado, AcaoCombate.atacar("J1", "I1", 1)))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.ACAO_INVALIDA);
	}

	@Test
	void defesaDobraDefesaEExpiraAposTurno() {
		Combatente soldado = jogador("J1", 0, 0, 30, 8, 3, 1, 3);
		Combatente goblin = inimigo("I1", 0, 1, 15, 9, 1, 1, 3);
		EstadoBatalha estado = novoEstado(List.of(soldado, goblin), 1, MAPA_SEM_OBSTACULOS);

		EstadoBatalha aposDefender = MotorCombate.processar(estado, AcaoCombate.defender("J1", 1));
		assertThat(aposDefender.buscar("J1").orElseThrow().defendendo()).isTrue();

		EstadoBatalha aposTurno = MotorCombate.processar(aposDefender, AcaoCombate.encerrarTurno(1));

		// dano = max(1, 9 - (3 * 2)) = 3
		assertThat(aposTurno.buscar("J1").orElseThrow().hp()).isEqualTo(27);
		assertThat(aposTurno.log()).contains("I1 atacou J1 causando 3 dano.");
		assertThat(aposTurno.turno()).isEqualTo(2);
		assertThat(aposTurno.buscar("J1").orElseThrow().defendendo()).isFalse();
	}

	@Test
	void iaSeAproximaEAtacaNoMesmoTurno() {
		Combatente soldado = jogador("J1", 0, 0, 30, 8, 3, 1, 3);
		Combatente goblin = inimigo("I1", 2, 0, 15, 6, 1, 1, 3);
		EstadoBatalha estado = novoEstado(List.of(soldado, goblin), 1, MAPA_SEM_OBSTACULOS);

		EstadoBatalha novoEstado = MotorCombate.processar(estado, AcaoCombate.encerrarTurno(1));

		Combatente goblinFinal = novoEstado.buscar("I1").orElseThrow();
		assertThat(goblinFinal.posicao().distanciaManhattan(new Posicao(0, 0))).isLessThanOrEqualTo(1);
		assertThat(novoEstado.buscar("J1").orElseThrow().hp()).isLessThan(30);
		assertThat(novoEstado.log()).anyMatch(linha -> linha.contains("I1 moveu"));
		assertThat(novoEstado.log()).anyMatch(linha -> linha.contains("I1 atacou J1"));
	}

	@Test
	void iaEscolheAlvoPorDistanciaDepoisHpDepoisId() {
		// J1 e J2 à mesma distância (2) do goblin; J2 tem menos HP.
		Combatente j1 = jogador("J1", 2, 0, 30, 8, 3, 1, 0);
		Combatente j2 = jogador("J2", 0, 2, 10, 8, 3, 1, 0);
		Combatente goblin = inimigo("I1", 0, 0, 15, 6, 1, 5, 0);
		EstadoBatalha estado = novoEstado(List.of(j1, j2, goblin), 1, MAPA_SEM_OBSTACULOS);

		EstadoBatalha novoEstado = MotorCombate.executarIA(estado);

		assertThat(novoEstado.buscar("J2").orElseThrow().hp()).isLessThan(10);
		assertThat(novoEstado.buscar("J1").orElseThrow().hp()).isEqualTo(30);
		assertThat(novoEstado.log()).anyMatch(linha -> linha.contains("I1 atacou J2"));
	}

	@Test
	void vitoriaQuandoUltimoInimigoMorre() {
		Combatente soldado = jogador("J1", 0, 0, 30, 20, 3, 1, 3);
		Combatente goblin = inimigo("I1", 0, 1, 5, 6, 1, 1, 3);
		EstadoBatalha estado = novoEstado(List.of(soldado, goblin), 1, MAPA_SEM_OBSTACULOS);

		EstadoBatalha novoEstado = MotorCombate.processar(estado, AcaoCombate.atacar("J1", "I1", 1));

		assertThat(MotorCombate.terminou(novoEstado)).isTrue();
		assertThat(MotorCombate.resultado(novoEstado)).isEqualTo(EstadoBatalha.Resultado.VITORIA);
		assertThat(novoEstado.buscar("I1")).isEmpty();
		assertThat(novoEstado.log()).contains("I1 morreu.");
	}

	@Test
	void derrotaPorTurnoMaximoComInimigosVivos() {
		Combatente soldado = jogador("J1", 0, 0, 30, 8, 3, 1, 0);
		Combatente goblin = inimigo("I1", 7, 7, 15, 6, 1, 1, 0);
		EstadoBatalha estado = new EstadoBatalha(List.of(soldado, goblin), 30, 30, MAPA_SEM_OBSTACULOS,
				EstadoBatalha.Resultado.NULO, List.of());

		EstadoBatalha novoEstado = MotorCombate.processar(estado, AcaoCombate.encerrarTurno(30));

		assertThat(MotorCombate.terminou(novoEstado)).isTrue();
		assertThat(MotorCombate.resultado(novoEstado)).isEqualTo(EstadoBatalha.Resultado.DERROTA);
	}

	@Test
	void renderEncerraBatalhaComDerrotaImediata() {
		Combatente soldado = jogador("J1", 0, 0, 30, 8, 3, 1, 3);
		EstadoBatalha estado = novoEstado(List.of(soldado), 1, MAPA_SEM_OBSTACULOS);

		EstadoBatalha novoEstado = MotorCombate.processar(estado, AcaoCombate.render(1));

		assertThat(MotorCombate.resultado(novoEstado)).isEqualTo(EstadoBatalha.Resultado.DERROTA);
		assertThat(novoEstado.log()).contains("Jogador se rendeu.");
	}

	@Test
	void turnoDesatualizadoRetorna409() {
		Combatente soldado = jogador("J1", 0, 0, 30, 8, 3, 1, 3);
		EstadoBatalha estado = novoEstado(List.of(soldado), 2, MAPA_SEM_OBSTACULOS);

		assertThatThrownBy(() -> MotorCombate.processar(estado, AcaoCombate.mover("J1", 1, 0, 1)))
				.isInstanceOf(RegraJogoException.class)
				.extracting(e -> ((RegraJogoException) e).getCodigo())
				.isEqualTo(CodigoErro.TURNO_DESATUALIZADO);
		assertThat(estado.turno()).isEqualTo(2);
		assertThat(estado.buscar("J1").orElseThrow().posicao()).isEqualTo(new Posicao(0, 0));
	}

	// ---- Fábricas auxiliares ----

	private static Combatente jogador(String id, int x, int y, int hp, int ataque, int defesa, int alcance,
			int movimento) {
		return new Combatente(id, Lado.JOGADOR, "Soldado", new Posicao(x, y), hp, hp, ataque, defesa, alcance,
				movimento, false, false, false, true);
	}

	private static Combatente inimigo(String id, int x, int y, int hp, int ataque, int defesa, int alcance,
			int movimento) {
		return new Combatente(id, Lado.INIMIGO, "Goblin", new Posicao(x, y), hp, hp, ataque, defesa, alcance,
				movimento, false, false, false, true);
	}

	private static EstadoBatalha novoEstado(List<Combatente> combatentes, int turno, MapaMasmorra mapa) {
		return new EstadoBatalha(combatentes, turno, 30, mapa, EstadoBatalha.Resultado.NULO, List.of());
	}

	private static MapaMasmorra mapa(int largura, int altura, List<MapaMasmorra.Posicao> obstaculos) {
		return new MapaMasmorra(largura, altura, obstaculos, List.of(), Map.of());
	}

}
