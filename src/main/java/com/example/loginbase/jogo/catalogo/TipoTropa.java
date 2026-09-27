package com.example.loginbase.jogo.catalogo;

/**
 * Tipos de tropa treináveis no quartel. Uma unidade = tipo + 1 arma do
 * modelo exigido + 1 armadura, ambas {@code DISPONIVEL}. Atributos finais:
 * ataque e alcance vêm da arma; defesa = {@link #defesaBase()} + defesa da
 * armadura; HP e movimento vêm do tipo.
 */
public enum TipoTropa {

	SOLDADO(ModeloItem.ESPADA, 30, 1, 3, 50, 60, 1),
	ARQUEIRO(ModeloItem.ARCO, 22, 0, 3, 50, 60, 2),
	LANCEIRO(ModeloItem.LANCA, 40, 2, 2, 60, 75, 3);

	private final ModeloItem armaExigida;
	private final int hp;
	private final int defesaBase;
	private final int movimento;
	private final long comida;
	private final int tempoTreinoSegundos;
	private final int nivelMinimoQuartel;

	TipoTropa(ModeloItem armaExigida, int hp, int defesaBase, int movimento, long comida,
			int tempoTreinoSegundos, int nivelMinimoQuartel) {
		this.armaExigida = armaExigida;
		this.hp = hp;
		this.defesaBase = defesaBase;
		this.movimento = movimento;
		this.comida = comida;
		this.tempoTreinoSegundos = tempoTreinoSegundos;
		this.nivelMinimoQuartel = nivelMinimoQuartel;
	}

	public ModeloItem armaExigida() {
		return armaExigida;
	}

	public int hp() {
		return hp;
	}

	public int defesaBase() {
		return defesaBase;
	}

	public int movimento() {
		return movimento;
	}

	public long comida() {
		return comida;
	}

	public int tempoTreinoSegundos() {
		return tempoTreinoSegundos;
	}

	public int nivelMinimoQuartel() {
		return nivelMinimoQuartel;
	}

}
