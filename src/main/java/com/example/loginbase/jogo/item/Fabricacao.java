package com.example.loginbase.jogo.item;

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
import lombok.Setter;

@Entity
@Table(name = "fabricacao")
@Getter
@Setter
@NoArgsConstructor
public class Fabricacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(name = "construcao_id", nullable = false, updatable = false)
	private Long construcaoId;

	@Column(name = "artesao_id", nullable = false)
	private Long artesaoId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ItemSubtipo subtipo;

	@Column(nullable = false)
	private int nivel;

	@Enumerated(EnumType.STRING)
	@Column(name = "atributo_escolhido", length = 3)
	private CodigoBonus atributoEscolhido;

	@Column(name = "item_id")
	private Long itemId;

	@Column(name = "pf_total", nullable = false)
	private int pfTotal;

	@Column(name = "pf_atual", nullable = false, precision = 10, scale = 4)
	private BigDecimal pfAtual = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoFabricacao estado = EstadoFabricacao.EM_ANDAMENTO;

	@Column(name = "turno_inicio", nullable = false)
	private int turnoInicio;

	@Column(name = "criado_em", nullable = false, updatable = false, insertable = false)
	private Instant criadoEm;

	public Fabricacao(Long vilaId, Long construcaoId, Long artesaoId, ItemSubtipo subtipo, int nivel, int pfTotal,
			int turnoInicio) {
		this.vilaId = vilaId;
		this.construcaoId = construcaoId;
		this.artesaoId = artesaoId;
		this.subtipo = subtipo;
		this.nivel = nivel;
		this.pfTotal = pfTotal;
		this.turnoInicio = turnoInicio;
	}
}
