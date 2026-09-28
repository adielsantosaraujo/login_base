package com.example.loginbase.jogo.dominio;

import com.example.loginbase.auditoria.EntidadeAuditavel;
import com.example.loginbase.jogo.catalogo.TipoTropa;

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
 * Unidade treinada de uma vila: {@link TipoTropa} + um item de arma e um de
 * armadura (cada {@link Item} só pode equipar uma unidade).
 */
@Entity
@Table(name = "jogo_unidades", uniqueConstraints = {
		@UniqueConstraint(columnNames = "arma_item_id"),
		@UniqueConstraint(columnNames = "armadura_item_id"),
		@UniqueConstraint(columnNames = { "vila_id", "nome", "sobrenome", "ordinal_nome" }) })
@Getter
@Setter
@NoArgsConstructor
public class Unidade extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false)
	private Long vilaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoTropa tipo;

	@Column(name = "arma_item_id", nullable = false)
	private Long armaItemId;

	@Column(name = "armadura_item_id", nullable = false)
	private Long armaduraItemId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusUnidade status;

	@Column(nullable = false, length = 60)
	private String nome;

	@Column(nullable = false, length = 60)
	private String sobrenome;

	@Column(name = "ordinal_nome", nullable = false)
	private int ordinalNome;

	/**
	 * Nome de exibição: {@code "Nome Sobrenome"}, ou {@code "Nome Sobrenome (N)"}
	 * quando {@link #ordinalNome} indicar um par nome/sobrenome duplicado na
	 * vila (N a partir de 2).
	 */
	public String nomeExibicao() {
		return ordinalNome == 1 ? nome + " " + sobrenome : nome + " " + sobrenome + " (" + ordinalNome + ")";
	}

}
