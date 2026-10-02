package com.example.loginbase.jogo.masmorra;

import static com.example.loginbase.jogo.batalha.TipoInimigo.*;

import com.example.loginbase.jogo.batalha.TipoInimigo;
import java.util.List;
import java.util.Optional;

/** Grupos de inimigos comuns e chefes por nível de masmorra (inimigos.md). */
public final class GrupoInimigos {

	private GrupoInimigos() {
	}

	/** Comuns sorteáveis no nível informado (N1–10). */
	public static List<TipoInimigo> comuns(int nivel) {
		if (nivel <= 2) {
			return List.of(RATO_GIGANTE, GOBLIN);
		}
		if (nivel <= 4) {
			return List.of(GOBLIN, GOBLIN_ARQUEIRO, LOBO);
		}
		if (nivel <= 6) {
			return List.of(LOBO, ESQUELETO, ESQUELETO_ARQUEIRO, ORC);
		}
		if (nivel <= 8) {
			return List.of(ESQUELETO_ARQUEIRO, ORC, XAMA_ORC, TROLL);
		}
		return List.of(ORC, XAMA_ORC, TROLL);
	}

	/** Chefe do nível; vazio abaixo de N3. */
	public static Optional<TipoInimigo> chefe(int nivel) {
		if (nivel < 3) {
			return Optional.empty();
		}
		if (nivel == 3) {
			return Optional.of(CHEFE_GOBLIN);
		}
		if (nivel <= 6) {
			return Optional.of(SENHOR_ORC);
		}
		if (nivel <= 9) {
			return Optional.of(TROLL_ANCIAO);
		}
		return Optional.of(DRAGAO_JOVEM);
	}
}
