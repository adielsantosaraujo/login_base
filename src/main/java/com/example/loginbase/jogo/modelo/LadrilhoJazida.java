package com.example.loginbase.jogo.modelo;

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
@Table(name = "ladrilho_jazida")
@IdClass(LadrilhoJazida.Chave.class)
@Getter
@Setter
@NoArgsConstructor
public class LadrilhoJazida {

	@Id
	@Column(name = "regiao_id")
	private Long regiaoId;

	@Id
	private Integer x;

	@Id
	private Integer y;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Jazida jazida;

	public LadrilhoJazida(Long regiaoId, Integer x, Integer y, Jazida jazida) {
		this.regiaoId = regiaoId;
		this.x = x;
		this.y = y;
		this.jazida = jazida;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	public static class Chave implements Serializable {

		private Long regiaoId;
		private Integer x;
		private Integer y;

		public Chave(Long regiaoId, Integer x, Integer y) {
			this.regiaoId = regiaoId;
			this.x = x;
			this.y = y;
		}

	}

}
