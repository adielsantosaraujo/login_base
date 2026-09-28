package com.example.loginbase.jogo.dominio;

import com.example.loginbase.auditoria.EntidadeAuditavel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contador histórico do maior {@code ordinalNome} já atribuído a um par
 * nome/sobrenome numa vila (escopo {@code (vilaId, nome, sobrenome)}). Nunca
 * é decrementado: a morte de uma {@link Unidade} apaga a unidade, não o
 * contador, preservando a numeração para futuras unidades com o mesmo par.
 */
@Entity
@Table(name = "jogo_contadores_nome", uniqueConstraints = @UniqueConstraint(columnNames = { "vila_id", "nome",
		"sobrenome" }))
@Getter
@Setter
@NoArgsConstructor
public class ContadorNome extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false)
	private Long vilaId;

	@Column(nullable = false, length = 60)
	private String nome;

	@Column(nullable = false, length = 60)
	private String sobrenome;

	@Column(name = "ultimo_ordinal", nullable = false)
	private int ultimoOrdinal;

}
