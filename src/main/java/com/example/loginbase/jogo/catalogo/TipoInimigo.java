package com.example.loginbase.jogo.catalogo;

/**
 * Tipos de inimigo enfrentados nas masmorras (combate tático).
 */
public enum TipoInimigo {

	GOBLIN(15, 6, 1, 1, 3),
	ESQUELETO_ARQUEIRO(12, 6, 0, 3, 2),
	ORC(30, 9, 3, 1, 2),
	TROLL(70, 13, 5, 1, 2);

	private final int hp;
	private final int ataque;
	private final int defesa;
	private final int alcance;
	private final int movimento;

	TipoInimigo(int hp, int ataque, int defesa, int alcance, int movimento) {
		this.hp = hp;
		this.ataque = ataque;
		this.defesa = defesa;
		this.alcance = alcance;
		this.movimento = movimento;
	}

	public int hp() {
		return hp;
	}

	public int ataque() {
		return ataque;
	}

	public int defesa() {
		return defesa;
	}

	public int alcance() {
		return alcance;
	}

	public int movimento() {
		return movimento;
	}

}
