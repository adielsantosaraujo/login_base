package com.example.loginbase.acesso;

import java.time.LocalDate;

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
 * Vínculo com vigência entre um {@link Usuario} e um {@link Perfil}. O
 * vínculo é vigente em uma data D quando {@code dataInicial <= D} e
 * ({@code dataFinal} é nula ou {@code dataFinal >= D}).
 */
@Entity
@Table(name = "usuario_rel_perfis")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioPerfil extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "perfil_id")
	private Perfil perfil;

	@Column(name = "data_inicial", nullable = false)
	private LocalDate dataInicial;

	@Column(name = "data_final")
	private LocalDate dataFinal;

}
