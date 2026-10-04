package com.example.loginbase.jogo.modelo;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vila_previa")
@Getter
@Setter
@NoArgsConstructor
public class VilaPrevia {

	@Id
	@Column(name = "usuario_id")
	private Long usuarioId;

	@Column(name = "previa_id", nullable = false, unique = true)
	private UUID previaId;

	@Column(nullable = false)
	private long semente;

	@Column(nullable = false)
	private int rodada;

	@Generated(event = EventType.INSERT)
	@Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime criadoEm;

	public VilaPrevia(Long usuarioId, UUID previaId, long semente, int rodada) {
		this.usuarioId = usuarioId;
		this.previaId = previaId;
		this.semente = semente;
		this.rodada = rodada;
	}

}
