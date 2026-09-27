package com.example.loginbase.jogo.dominio;

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
 * Estoque de sementes de um {@link Cultivo} em uma vila (tabela
 * {@code jogo_sementes}).
 */
@Entity
@Table(name = "jogo_sementes", uniqueConstraints = @UniqueConstraint(columnNames = { "vila_id", "cultivo" }))
@Getter
@Setter
@NoArgsConstructor
public class EstoqueSemente extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false)
	private Long vilaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Cultivo cultivo;

	@Column(nullable = false)
	private int quantidade;

}
