package com.example.loginbase.jogo.cidadao;

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
@Table(name = "familia")
@Getter
@Setter
@NoArgsConstructor
public class Familia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(nullable = false, length = 100)
	private String sobrenome;

	@Column(name = "casa_id")
	private Long casaId;

	public Familia(Long vilaId, String sobrenome, Long casaId) {
		this.vilaId = vilaId;
		this.sobrenome = sobrenome;
		this.casaId = casaId;
	}

}
