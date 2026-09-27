package com.example.loginbase.jogo.masmorra.combate;

/**
 * Ação submetida a {@link MotorCombate#processar(EstadoBatalha, AcaoCombate)}.
 * Cada ação inclui o número do turno esperado; se não corresponder ao turno
 * atual da batalha, o motor rejeita com {@code TURNO_DESATUALIZADO} (409).
 *
 * @param tipo          tipo da ação.
 * @param combatenteId  combatente que age (irrelevante para
 *                      {@code ENCERRAR_TURNO}/{@code RENDER}).
 * @param x             coluna de destino (apenas {@code MOVER}).
 * @param y             linha de destino (apenas {@code MOVER}).
 * @param alvoId        combatente alvo (apenas {@code ATACAR}).
 * @param turno         turno em que a ação foi decidida pelo cliente.
 */
public record AcaoCombate(TipoAcao tipo, String combatenteId, int x, int y, String alvoId, int turno) {

	public enum TipoAcao {
		MOVER,
		ATACAR,
		DEFENDER,
		ENCERRAR_TURNO,
		RENDER
	}

	public static AcaoCombate mover(String combatenteId, int x, int y, int turno) {
		return new AcaoCombate(TipoAcao.MOVER, combatenteId, x, y, null, turno);
	}

	public static AcaoCombate atacar(String combatenteId, String alvoId, int turno) {
		return new AcaoCombate(TipoAcao.ATACAR, combatenteId, 0, 0, alvoId, turno);
	}

	public static AcaoCombate defender(String combatenteId, int turno) {
		return new AcaoCombate(TipoAcao.DEFENDER, combatenteId, 0, 0, null, turno);
	}

	public static AcaoCombate encerrarTurno(int turno) {
		return new AcaoCombate(TipoAcao.ENCERRAR_TURNO, null, 0, 0, null, turno);
	}

	public static AcaoCombate render(int turno) {
		return new AcaoCombate(TipoAcao.RENDER, null, 0, 0, null, turno);
	}

}
