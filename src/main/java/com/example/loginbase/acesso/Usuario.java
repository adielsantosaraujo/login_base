package com.example.loginbase.acesso;

import com.example.loginbase.auditoria.EntidadeAuditavel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuário do controle de acesso. E-mail e celular são sempre gravados
 * normalizados (ver {@link NormalizacaoContato}), tanto pelos setters quanto
 * pelo callback {@link #normalizar()}, para cobrir também os casos em que os
 * campos são preenchidos via construtor/reflexão do Hibernate.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String nome;

	@Column(nullable = false, length = 150)
	private String email;

	@Column(nullable = false, length = 255)
	private String senha;

	@Column(length = 11)
	private String celular;

	public void setEmail(String email) {
		this.email = NormalizacaoContato.email(email);
	}

	public void setCelular(String celular) {
		if (celular == null || celular.isBlank()) {
			this.celular = null;
			return;
		}

		String digitos = NormalizacaoContato.digitos(celular);
		if (digitos.length() != NormalizacaoContato.DIGITOS_CELULAR) {
			throw new IllegalArgumentException("Celular deve ter 11 dígitos (DDD + número)");
		}

		this.celular = digitos;
	}

	@PrePersist
	@PreUpdate
	void normalizar() {
		setEmail(this.email);
		setCelular(this.celular);
	}

}
