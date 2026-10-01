package com.example.loginbase.jogo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "regiao")
@Getter
@Setter
@NoArgsConstructor
public class Regiao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(nullable = false, updatable = false)
	private Integer indice;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private TipoRegiao tipo;

	@Column(nullable = false)
	private boolean possuida;

	@Column(name = "limpa_ate_turno")
	private Integer limpaAteTurno;

	public Regiao(Long vilaId, Integer indice) {
		this.vilaId = vilaId;
		this.indice = indice;
	}

}
