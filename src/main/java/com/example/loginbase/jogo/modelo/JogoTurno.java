package com.example.loginbase.jogo.modelo;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jogo_turno")
@Getter
@Setter
@NoArgsConstructor
public class JogoTurno {

	@Id
	private Integer numero;

	@Column(name = "iniciado_em", nullable = false)
	private Instant iniciadoEm;

	@Column(name = "concluido_em")
	private Instant concluidoEm;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusTurno status = StatusTurno.PROCESSANDO;

	public JogoTurno(Integer numero, Instant iniciadoEm) {
		this.numero = numero;
		this.iniciadoEm = iniciadoEm;
	}

}
