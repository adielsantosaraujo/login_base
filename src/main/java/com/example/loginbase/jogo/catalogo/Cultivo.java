package com.example.loginbase.jogo.catalogo;

/**
 * Cultivos plantáveis nos canteiros da fazenda. Plantio é instantâneo e
 * consome semente, exceto {@code TRIGO}, que não exige semente (infinita).
 */
public enum Cultivo {

	TRIGO(20, false, 0),
	MILHO(30, true, 1),
	BATATA(45, true, 2),
	ABOBORA_DOURADA(70, true, 4);

	private final long producaoComidaPorHora;
	private final boolean exigeSemente;
	private final int nivelMasmorraParaSemente;

	Cultivo(long producaoComidaPorHora, boolean exigeSemente, int nivelMasmorraParaSemente) {
		this.producaoComidaPorHora = producaoComidaPorHora;
		this.exigeSemente = exigeSemente;
		this.nivelMasmorraParaSemente = nivelMasmorraParaSemente;
	}

	public long producaoComidaPorHora() {
		return producaoComidaPorHora;
	}

	public boolean exigeSemente() {
		return exigeSemente;
	}

	/** Nível mínimo de masmorra vencido para obter a semente (0 = não se aplica). */
	public int nivelMasmorraParaSemente() {
		return nivelMasmorraParaSemente;
	}

}
