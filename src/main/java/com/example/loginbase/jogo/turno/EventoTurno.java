package com.example.loginbase.jogo.turno;

import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

@Entity
@Table(name = "evento_turno")
@Getter
@Setter
@NoArgsConstructor
public class EventoTurno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(nullable = false, updatable = false)
	private Integer turno;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40, updatable = false)
	private TipoEventoTurno tipo;

	@Column(length = 500)
	private String mensagem;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> dados;

	@Column(name = "criado_em", nullable = false, updatable = false, insertable = false)
	private Instant criadoEm;

	public EventoTurno(Long vilaId, Integer turno, TipoEventoTurno tipo, String mensagem,
			Map<String, Object> dados) {
		this.vilaId = vilaId;
		this.turno = turno;
		this.tipo = tipo;
		this.mensagem = mensagem;
		this.dados = dados;
	}

}
