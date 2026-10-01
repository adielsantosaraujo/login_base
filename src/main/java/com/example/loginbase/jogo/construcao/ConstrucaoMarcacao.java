package com.example.loginbase.jogo.construcao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "construcao_marcacao")
@Getter
@Setter
@NoArgsConstructor
public class ConstrucaoMarcacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(name = "regiao_indice", nullable = false, updatable = false)
	private Integer regiaoIndice;

	@Column(nullable = false, updatable = false)
	private Integer x;

	@Column(nullable = false, updatable = false)
	private Integer y;

	@Column(name = "construcao_id", nullable = false, updatable = false)
	private Long construcaoId;

	public ConstrucaoMarcacao(Long vilaId, Integer regiaoIndice, Integer x, Integer y, Long construcaoId) {
		this.vilaId = vilaId;
		this.regiaoIndice = regiaoIndice;
		this.x = x;
		this.y = y;
		this.construcaoId = construcaoId;
	}

}
