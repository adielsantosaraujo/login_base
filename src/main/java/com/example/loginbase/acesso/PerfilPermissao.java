package com.example.loginbase.acesso;

import com.example.loginbase.auditoria.EntidadeAuditavel;

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
 * Vínculo entre um {@link Perfil} e uma {@link Permissao}.
 */
@Entity
@Table(name = "perfis_rel_permissoes")
@Getter
@Setter
@NoArgsConstructor
public class PerfilPermissao extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "perfil_id")
	private Perfil perfil;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "permissao_id")
	private Permissao permissao;

}
