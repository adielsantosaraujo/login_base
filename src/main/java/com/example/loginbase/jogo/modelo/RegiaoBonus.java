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
@Table(name = "regiao_bonus")
@Getter
@Setter
@NoArgsConstructor
public class RegiaoBonus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "regiao_id", nullable = false, updatable = false)
	private long regiaoId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BonusRegiao bonus;

	@JdbcTypeCode(SqlTypes.SMALLINT)
	@Column(nullable = false)
	private int posicao;

	@Column(nullable = false)
	private int valor;

	public RegiaoBonus(long regiaoId, BonusRegiao bonus, int posicao, int valor) {
		this.regiaoId = regiaoId;
		this.bonus = bonus;
		this.posicao = posicao;
		this.valor = valor;
	}

}
