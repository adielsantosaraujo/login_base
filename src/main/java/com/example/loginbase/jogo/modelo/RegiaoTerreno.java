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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "regiao_terreno")
@Getter
@Setter
@NoArgsConstructor
public class RegiaoTerreno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "regiao_id", nullable = false, updatable = false)
	private long regiaoId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoTerreno terreno;

	@JdbcTypeCode(SqlTypes.SMALLINT)
	@Column(nullable = false)
	private int posicao;

	@JdbcTypeCode(SqlTypes.SMALLINT)
	@Column(nullable = false)
	private int percentual;

	public RegiaoTerreno(long regiaoId, TipoTerreno terreno, int posicao, int percentual) {
		this.regiaoId = regiaoId;
		this.terreno = terreno;
		this.posicao = posicao;
		this.percentual = percentual;
	}

}
