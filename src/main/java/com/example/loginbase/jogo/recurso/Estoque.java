package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;

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
@Table(name = "estoque")
@Getter
@Setter
@NoArgsConstructor
public class Estoque {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30, updatable = false)
	private Recurso recurso;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal quantidade = BigDecimal.ZERO;

	public Estoque(Long vilaId, Recurso recurso, BigDecimal quantidade) {
		this.vilaId = vilaId;
		this.recurso = recurso;
		this.quantidade = quantidade;
	}

}
