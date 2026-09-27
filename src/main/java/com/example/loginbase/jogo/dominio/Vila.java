package com.example.loginbase.jogo.dominio;

import java.time.Instant;

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
 * Vila de um usuário: recursos (comida/madeira/pedra/ferro, em milésimos) e
 * nível de masmorra liberado. Uma vila por usuário. Prédios, canteiros,
 * estoques de semente, itens, unidades, ordens e batalhas referenciam a vila
 * por {@code vilaId} (mapeamento simples, sem coleções bidirecionais).
 */
@Entity
@Table(name = "jogo_vilas", uniqueConstraints = @UniqueConstraint(columnNames = "usuario_id"))
@Getter
@Setter
@NoArgsConstructor
public class Vila extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "usuario_id", nullable = false)
	private Long usuarioId;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(nullable = false)
	private long comida;

	@Column(nullable = false)
	private long madeira;

	@Column(nullable = false)
	private long pedra;

	@Column(nullable = false)
	private long ferro;

	@Column(name = "recursos_atualizados_em", nullable = false)
	private Instant recursosAtualizadosEm;

	@Column(name = "masmorra_nivel_liberado", nullable = false)
	private int masmorraNivelLiberado = 1;

}
