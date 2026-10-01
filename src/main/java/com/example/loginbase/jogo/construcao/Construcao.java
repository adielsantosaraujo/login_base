package com.example.loginbase.jogo.construcao;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "construcao")
@Getter
@Setter
@NoArgsConstructor
public class Construcao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private TipoConstrucao tipo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 2)
	private NivelConstrucao nivel;

	@Column(name = "regiao_indice", nullable = false)
	private Integer regiaoIndice;

	@Column(nullable = false)
	private Integer x;

	@Column(nullable = false)
	private Integer y;

	@Column(nullable = false)
	private Integer tamanho;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoConstrucao estado;

	@Column(name = "po_total", nullable = false)
	private Integer poTotal;

	@Column(name = "po_atual", nullable = false)
	private Integer poAtual;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private String configuracao;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private Instant criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private Instant atualizadoEm;

	@PrePersist
	void aoCriar() {
		criadoEm = Instant.now();
		atualizadoEm = criadoEm;
	}

	@PreUpdate
	void aoAtualizar() {
		atualizadoEm = Instant.now();
	}

}
