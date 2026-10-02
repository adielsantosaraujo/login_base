package com.example.loginbase.jogo.batalha;

import java.util.List;

/** Regras de alcance R4/R5: quem o atacante pode atingir entre os inimigos vivos. */
public final class ValidadorAlvo {

	private ValidadorAlvo() {
	}

	/**
	 * Alvos permitidos entre {@code oponentesVivos} (o chamador já exclui os abatidos). Lista vazia = sem alvo válido.
	 * Corpo a corpo (frente ou flex) só atinge a frente adversária enquanto houver vivo nela; senão libera a
	 * retaguarda. CORPO_A_CORPO_FRENTE (espada) ainda exige que o portador esteja na frente. Distância atinge todos.
	 */
	public static List<Combatente> alvosPermitidos(Combatente atacante, List<Combatente> oponentesVivos) {
		switch (atacante.alcance()) {
			case DISTANCIA:
				return List.copyOf(oponentesVivos);
			case CORPO_A_CORPO_FRENTE:
				if (atacante.linha() != LinhaCombate.FRENTE) {
					return List.of();
				}
				return frenteOuTodos(oponentesVivos);
			case CORPO_A_CORPO_FLEX:
				return frenteOuTodos(oponentesVivos);
			default:
				return List.of();
		}
	}

	private static List<Combatente> frenteOuTodos(List<Combatente> vivos) {
		List<Combatente> frente = vivos.stream().filter(c -> c.linha() == LinhaCombate.FRENTE).toList();
		return frente.isEmpty() ? List.copyOf(vivos) : frente;
	}
}
