package com.example.loginbase.jogo.masmorra;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Masmorra de uma região da vila; inativas (vencidas) ficam no histórico. */
@Entity
@Table(name = "masmorra")
@Getter
@Setter
@NoArgsConstructor
public class Masmorra {

	public static final int NIVEL_MIN = 1;
	public static final int NIVEL_MAX = 10;
	public static final int REGIAO_MIN = 1;
	public static final int REGIAO_MAX = 16;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(name = "regiao_indice", nullable = false, updatable = false)
	private int regiaoIndice;

	@Column(nullable = false)
	private int nivel;

	@Column(name = "turno_surgimento", nullable = false, updatable = false)
	private int turnoSurgimento;

	@Column(name = "turnos_sem_ataque", nullable = false)
	private int turnosSemAtaque;

	/** Turno do último ataque; a evolução não incrementa o contador no turno do ataque. */
	@Column(name = "turno_ultimo_ataque")
	private Integer turnoUltimoAtaque;

	@Column(nullable = false)
	private boolean ativa = true;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private Instant criadoEm;

	public Masmorra(Long vilaId, int regiaoIndice, int nivel, int turnoSurgimento) {
		validarRegiao(regiaoIndice);
		validarNivel(nivel);
		this.vilaId = vilaId;
		this.regiaoIndice = regiaoIndice;
		this.nivel = nivel;
		this.turnoSurgimento = turnoSurgimento;
	}

	public void setNivel(int nivel) {
		validarNivel(nivel);
		this.nivel = nivel;
	}

	public void setRegiaoIndice(int regiaoIndice) {
		validarRegiao(regiaoIndice);
		this.regiaoIndice = regiaoIndice;
	}

	private static void validarNivel(int nivel) {
		if (nivel < NIVEL_MIN || nivel > NIVEL_MAX) {
			throw new IllegalArgumentException("Nível da masmorra deve estar entre 1 e 10: " + nivel);
		}
	}

	private static void validarRegiao(int regiaoIndice) {
		if (regiaoIndice < REGIAO_MIN || regiaoIndice > REGIAO_MAX) {
			throw new IllegalArgumentException("Região da masmorra deve estar entre 1 e 16: " + regiaoIndice);
		}
	}

	@PrePersist
	void aoCriar() {
		criadoEm = Instant.now();
	}

}
