package com.example.loginbase.jogo.pedra;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "pedra")
@Getter
@Setter
@NoArgsConstructor
public class Pedra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoPedra qualidade;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private List<BonusPedra> bonus = new ArrayList<>();

	@Column(name = "item_id")
	private Long itemId;

	@Column(name = "criada_em", nullable = false, updatable = false, insertable = false)
	private Instant criadaEm;

	public Pedra(Long vilaId, TipoPedra qualidade, List<BonusPedra> bonus) {
		this.vilaId = vilaId;
		this.qualidade = qualidade;
		this.bonus = bonus == null ? new ArrayList<>() : new ArrayList<>(bonus);
	}
}
