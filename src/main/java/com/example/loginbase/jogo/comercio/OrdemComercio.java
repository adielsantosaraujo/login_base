package com.example.loginbase.jogo.comercio;

import java.math.BigDecimal;
import java.time.Instant;

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

import com.example.loginbase.jogo.recurso.Recurso;

@Entity
@Table(name = "ordem_comercio")
@Getter
@NoArgsConstructor
public class OrdemComercio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(nullable = false, updatable = false)
	private int turno;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10, updatable = false)
	private TipoOrdem tipo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30, updatable = false)
	private Recurso recurso;

	@Column(nullable = false, updatable = false)
	private int quantidade;

	@Column(name = "preco_unitario", nullable = false, updatable = false)
	private BigDecimal precoUnitario;

	@Column(name = "ouro_total", nullable = false, updatable = false)
	private BigDecimal ouroTotal;

	@Column(name = "criado_em", nullable = false, updatable = false, insertable = false)
	private Instant criadoEm;

	public OrdemComercio(Long vilaId, int turno, TipoOrdem tipo, Recurso recurso, int quantidade,
			BigDecimal precoUnitario, BigDecimal ouroTotal) {
		this.vilaId = vilaId;
		this.turno = turno;
		this.tipo = tipo;
		this.recurso = recurso;
		this.quantidade = quantidade;
		this.precoUnitario = precoUnitario;
		this.ouroTotal = ouroTotal;
	}

}
