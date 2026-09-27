package com.example.loginbase.jogo.masmorra.combate;

/**
 * Combatente (do jogador ou inimigo) numa batalha tática. Imutável: cada
 * método de "ação" devolve um novo {@link Combatente}, sem mutar o anterior.
 *
 * @param id            identificador único na batalha (ex.: {@code "J1"},
 *                      {@code "I3"}), usado no log e nas ações.
 * @param lado          {@link Lado#JOGADOR} ou {@link Lado#INIMIGO}.
 * @param tipoOuUnidade rótulo descritivo (ex.: nome do {@code TipoInimigo}
 *                      ou da unidade do jogador), sem uso nas regras.
 * @param posicao       posição atual no grid.
 * @param hp            pontos de vida atuais.
 * @param hpMax         pontos de vida máximos.
 * @param ataque        dano base de ataque.
 * @param defesa        defesa base (dobrada quando {@code defendendo}).
 * @param alcance       alcance de ataque, em distância Manhattan.
 * @param movimento     casas que pode percorrer por turno (BFS).
 * @param defendendo    se está em posição defensiva (defesa dobrada) até o
 *                      início do próximo turno do jogador.
 * @param moveu         se já se moveu neste turno.
 * @param agiu          se já atacou ou defendeu neste turno.
 * @param vivo          se está vivo (hp > 0).
 */
public record Combatente(String id, Lado lado, String tipoOuUnidade, Posicao posicao, int hp, int hpMax, int ataque,
		int defesa, int alcance, int movimento, boolean defendendo, boolean moveu, boolean agiu, boolean vivo) {

	/** Defesa efetiva: dobrada se {@code defendendo}, senão a defesa base. */
	public int defesaEfetiva() {
		return defendendo ? defesa * 2 : defesa;
	}

	/** Novo combatente com o HP reduzido por {@code dano} (mínimo 0). */
	public Combatente recebeDano(int dano) {
		int novoHp = Math.max(0, hp - dano);
		return new Combatente(id, lado, tipoOuUnidade, posicao, novoHp, hpMax, ataque, defesa, alcance, movimento,
				defendendo, moveu, agiu, novoHp > 0);
	}

	/** Novo combatente na {@code novaPosicao}, marcado como já tendo movido. */
	public Combatente moverPara(Posicao novaPosicao) {
		return new Combatente(id, lado, tipoOuUnidade, novaPosicao, hp, hpMax, ataque, defesa, alcance, movimento,
				defendendo, true, agiu, vivo);
	}

	/** Novo combatente marcado como tendo agido (atacou) neste turno. */
	public Combatente atacar() {
		return new Combatente(id, lado, tipoOuUnidade, posicao, hp, hpMax, ataque, defesa, alcance, movimento,
				defendendo, moveu, true, vivo);
	}

	/** Novo combatente em posição defensiva (defesa dobrada), já tendo agido. */
	public Combatente defender() {
		return new Combatente(id, lado, tipoOuUnidade, posicao, hp, hpMax, ataque, defesa, alcance, movimento, true,
				moveu, true, vivo);
	}

	/** Novo combatente com as flags de turno (moveu/agiu/defendendo) zeradas. */
	public Combatente resetarTurno() {
		return new Combatente(id, lado, tipoOuUnidade, posicao, hp, hpMax, ataque, defesa, alcance, movimento, false,
				false, false, vivo);
	}

}
