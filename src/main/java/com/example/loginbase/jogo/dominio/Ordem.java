package com.example.loginbase.jogo.dominio;

import java.time.Instant;

import com.example.loginbase.auditoria.EntidadeAuditavel;

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
 * Ordem na fila de produção de uma vila: construção, forja ou treino. Uma
 * vila tem no máximo uma ordem pendente por {@link CategoriaOrdem} (fila de
 * 1). {@code alvo} guarda o nome do tipo/modelo alvo (ex.: {@code TipoPredio}
 * ou {@code ModeloItem}), e {@code nivel}/{@code armaItemId}/
 * {@code armaduraItemId} são usados conforme a categoria.
 */
@Entity
@Table(name = "jogo_ordens", uniqueConstraints = @UniqueConstraint(columnNames = { "vila_id", "categoria" }))
@Getter
@Setter
@NoArgsConstructor
public class Ordem extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false)
	private Long vilaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CategoriaOrdem categoria;

	@Column(nullable = false, length = 30)
	private String alvo;

	@Column
	private Integer nivel;

	@Column(nullable = false)
	private int quantidade = 1;

	@Column(name = "arma_item_id")
	private Long armaItemId;

	@Column(name = "armadura_item_id")
	private Long armaduraItemId;

	@Column(name = "iniciada_em", nullable = false)
	private Instant iniciadaEm;

	@Column(name = "conclui_em", nullable = false)
	private Instant concluiEm;

}
