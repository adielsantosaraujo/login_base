package com.example.loginbase.auditoria;

import java.time.Instant;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Superclasse com os campos de auditoria comuns a todas as entidades de
 * controle de acesso: quem criou/alterou o registro e quando.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadeAuditavel {

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant criadoEm;

	@CreatedBy
	@Column(nullable = false, updatable = false, length = 150)
	private String criadoPor;

	@LastModifiedDate
	@Column(nullable = false)
	private Instant alteradoEm;

	@LastModifiedBy
	@Column(nullable = false, length = 150)
	private String alteradoPor;

}
