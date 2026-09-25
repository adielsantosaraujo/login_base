package com.example.loginbase.acesso;

import java.time.Instant;

import com.example.loginbase.auditoria.EntidadeAuditavel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de uma sessão HTTP autenticada. {@code dataFim} nula indica
 * sessão aberta. O {@code token} é derivado do identificador da sessão HTTP
 * (nunca o identificador em texto puro).
 */
@Entity
@Table(name = "sessoes")
@Getter
@Setter
@NoArgsConstructor
public class Sessao extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(name = "data_inicio", nullable = false)
	private Instant dataInicio;

	@Column(name = "data_fim")
	private Instant dataFim;

	@Column(nullable = false, length = 64)
	private String token;

	@Column(length = 45)
	private String ip;

	@Column(length = 500)
	private String dispositivo;

}
