package com.example.loginbase.jogo.cidadao;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cidadao_profissao")
@IdClass(CidadaoProfissao.Chave.class)
@Getter
@Setter
@NoArgsConstructor
public class CidadaoProfissao {

	@Id
	@Column(name = "cidadao_id")
	private Long cidadaoId;

	@Id
	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private Profissao profissao;

	@Column(name = "pontos_base", nullable = false)
	private int pontosBase;

	@Column(name = "turnos_experiencia", nullable = false)
	private int turnosExperiencia;

	public CidadaoProfissao(Long cidadaoId, Profissao profissao, int pontosBase) {
		this.cidadaoId = cidadaoId;
		this.profissao = profissao;
		this.pontosBase = pontosBase;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class Chave implements Serializable {
		private Long cidadaoId;
		private Profissao profissao;
	}

}
