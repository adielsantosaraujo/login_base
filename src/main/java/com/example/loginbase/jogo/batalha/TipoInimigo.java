package com.example.loginbase.jogo.batalha;

import com.example.loginbase.jogo.item.catalogo.Alcance;

/** Inimigos da tabela 8.3 de batalha.md. Atributos base no N1; multiplicados por M(N). */
public enum TipoInimigo {
	RATO_GIGANTE("Rato gigante", 25, 12, 4, 8, LinhaCombate.FRENTE, false),
	GOBLIN("Goblin", 40, 16, 8, 10, LinhaCombate.FRENTE, false),
	GOBLIN_ARQUEIRO("Goblin arqueiro", 30, 15, 5, 11, LinhaCombate.RETAGUARDA, false),
	LOBO("Lobo", 35, 18, 6, 14, LinhaCombate.FRENTE, false),
	ESQUELETO("Esqueleto", 55, 22, 14, 6, LinhaCombate.FRENTE, false),
	ESQUELETO_ARQUEIRO("Esqueleto arqueiro", 40, 20, 10, 8, LinhaCombate.RETAGUARDA, false),
	ORC("Orc", 85, 28, 18, 7, LinhaCombate.FRENTE, false),
	XAMA_ORC("Xamã orc", 60, 26, 10, 9, LinhaCombate.RETAGUARDA, true),
	TROLL("Troll", 150, 38, 24, 3, LinhaCombate.FRENTE, false),
	CHEFE_GOBLIN("Chefe goblin", 120, 24, 12, 10, LinhaCombate.FRENTE, false),
	SENHOR_ORC("Senhor orc", 220, 36, 24, 8, LinhaCombate.FRENTE, false),
	TROLL_ANCIAO("Troll ancião", 380, 48, 30, 4, LinhaCombate.FRENTE, false),
	DRAGAO_JOVEM("Dragão jovem", 600, 60, 40, 12, LinhaCombate.FRENTE, false);

	/** Crítico fixo dos inimigos (pp). */
	public static final double CRITICO_PP = 5;

	private final String nome;
	private final int pv;
	private final int atq;
	private final int def;
	private final int ini;
	private final LinhaCombate linha;
	private final boolean ignoraDefesa25;

	TipoInimigo(String nome, int pv, int atq, int def, int ini, LinhaCombate linha, boolean ignoraDefesa25) {
		this.nome = nome;
		this.pv = pv;
		this.atq = atq;
		this.def = def;
		this.ini = ini;
		this.linha = linha;
		this.ignoraDefesa25 = ignoraDefesa25;
	}

	public String nome() {
		return nome;
	}

	public int pvBase() {
		return pv;
	}

	public int atqBase() {
		return atq;
	}

	public int defBase() {
		return def;
	}

	public int iniBase() {
		return ini;
	}

	public LinhaCombate linha() {
		return linha;
	}

	public boolean ignoraDefesa25() {
		return ignoraDefesa25;
	}

	/** M(N) = 1 + 0,15 x (N - 1). */
	public static double multiplicador(int nivelMasmorra) {
		return 1 + 0.15 * (nivelMasmorra - 1);
	}

	/** Atributos escalados por M(N); PV e iniciativa arredondados ao inteiro mais próximo. */
	public AtributosCombate atributos(int nivelMasmorra) {
		double m = multiplicador(nivelMasmorra);
		return new AtributosCombate((int) Math.round(pv * m), atq * m, def * m, (int) Math.round(ini * m),
				CRITICO_PP);
	}

	/** Cria o combatente inimigo; linha Frente = corpo a corpo, Retaguarda = distância; VEL 0. */
	public Combatente criar(long indice, int nivelMasmorra) {
		AtributosCombate a = atributos(nivelMasmorra);
		Alcance alcance = linha == LinhaCombate.FRENTE ? Alcance.CORPO_A_CORPO_FRENTE : Alcance.DISTANCIA;
		return new Combatente(indice, LadoCombate.INIMIGO, nome, linha, a.pvMax(), a.ataque(), a.defesa(),
				a.iniciativaBase(), a.criticoPp(), 0, alcance, ignoraDefesa25);
	}
}
