package com.example.loginbase.acesso;

import com.example.loginbase.auditoria.EntidadeAuditavel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Perfil de acesso (por exemplo, {@code ADMIN}), ao qual usuários são
 * vinculados com vigência e que agrega permissões.
 */
@Entity
@Table(name = "perfis")
@Getter
@Setter
@NoArgsConstructor
public class Perfil extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(length = 255)
	private String descricao;

}
