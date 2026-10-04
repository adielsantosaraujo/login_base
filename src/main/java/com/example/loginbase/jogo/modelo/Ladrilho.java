package com.example.loginbase.jogo.modelo;

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
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ladrilho")
@Getter
@Setter
@NoArgsConstructor
public class Ladrilho {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "regiao_id", nullable = false, updatable = false)
	private long regiaoId;

	@JdbcTypeCode(SqlTypes.SMALLINT)
	@Column(nullable = false)
	private int x;

	@JdbcTypeCode(SqlTypes.SMALLINT)
	@Column(nullable = false)
	private int y;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoTerreno terreno;

	@JdbcTypeCode(SqlTypes.SMALLINT)
	@Column(name = "bonus_base", nullable = false)
	private int bonusBase;

	@JdbcTypeCode(SqlTypes.SMALLINT)
	@Column(name = "bonus_adjacente", nullable = false)
	private int bonusAdjacente;

	public Ladrilho(long regiaoId, int x, int y, TipoTerreno terreno, int bonusBase, int bonusAdjacente) {
		this.regiaoId = regiaoId;
		this.x = x;
		this.y = y;
		this.terreno = terreno;
		this.bonusBase = bonusBase;
		this.bonusAdjacente = bonusAdjacente;
	}

	@Transient
	public int getBonusTotal() {
		return bonusBase + bonusAdjacente;
	}

}
