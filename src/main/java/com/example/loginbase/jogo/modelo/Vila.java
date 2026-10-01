package com.example.loginbase.jogo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vila")
@Getter
@Setter
@NoArgsConstructor
public class Vila {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id", nullable = false, updatable = false)
	private Long usuarioId;

	@Column(length = 255)
	private String nome;

	@Column(nullable = false)
	private Long semente;

	@Column(name = "turno_criacao", nullable = false)
	private Integer turnoCriacao;

	@Column(name = "familia_lider_id")
	private Long familiaLiderId;

	@Column(name = "bem_alimentada", nullable = false)
	private boolean bemAlimentada;

	@Column(name = "populacao_confirmada", nullable = false)
	private boolean populacaoConfirmada;

	/** Último turno processado para esta vila; garante a idempotência do pipeline. */
	@Column(name = "turno_processado")
	private Integer turnoProcessado;

	@Version
	@Column(nullable = false)
	private int version;

	public Vila(Long usuarioId, String nome, Long semente, Integer turnoCriacao) {
		this.usuarioId = usuarioId;
		this.nome = nome;
		this.semente = semente;
		this.turnoCriacao = turnoCriacao;
	}

}
