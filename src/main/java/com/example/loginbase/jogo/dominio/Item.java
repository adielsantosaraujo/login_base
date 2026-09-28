package com.example.loginbase.jogo.dominio;

import com.example.loginbase.auditoria.EntidadeAuditavel;
import com.example.loginbase.jogo.catalogo.ModeloItem;

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

/**
 * Item (arma ou armadura) de uma vila: {@link ModeloItem}, nível (1–5),
 * origem ({@link OrigemItem}) e status ({@link StatusItem}).
 */
@Entity
@Table(name = "jogo_itens")
@Getter
@Setter
@NoArgsConstructor
public class Item extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false)
	private Long vilaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ModeloItem modelo;

	@Column(nullable = false)
	private int nivel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrigemItem origem;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusItem status;

	@Column(name = "ordem_id")
	private Long ordemId;

}
