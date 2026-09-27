package com.example.loginbase.jogo.dominio;

import java.time.Instant;

import com.example.loginbase.auditoria.EntidadeAuditavel;
import com.example.loginbase.jogo.catalogo.Cultivo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Canteiro da fazenda de uma vila: posição de 1 a 5, com o {@link Cultivo}
 * plantado e o instante do plantio.
 */
@Entity
@Table(name = "jogo_canteiros", uniqueConstraints = @UniqueConstraint(columnNames = { "vila_id", "posicao" }))
@Getter
@Setter
@NoArgsConstructor
public class Canteiro extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false)
	private Long vilaId;

	@Column(nullable = false)
	private int posicao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Cultivo cultivo;

	@Column(name = "plantado_em", nullable = false)
	private Instant plantadoEm;

}
