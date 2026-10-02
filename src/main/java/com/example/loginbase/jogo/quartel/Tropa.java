package com.example.loginbase.jogo.quartel;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tropa de guerreiros; os membros são os cidadãos com {@code tropa_id} igual ao id da tropa. */
@Entity
@Table(name = "tropa")
@Getter
@Setter
@NoArgsConstructor
public class Tropa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(name = "quartel_id", nullable = false, updatable = false)
	private Long quartelId;

	@Column(nullable = false, length = 100)
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoTropa estado = EstadoTropa.AQUARTELADA;

	/** Sem FK: masmorras só existem a partir da Fase 5. */
	@Column(name = "masmorra_id")
	private Long masmorraId;

	@Column(name = "regiao_destino")
	private Integer regiaoDestino;

	@Column(name = "turnos_viagem")
	private Integer turnosViagem;

	@Column(name = "turnos_restantes")
	private Integer turnosRestantes;

	/** Última batalha da tropa (FK com on delete set null). */
	@Column(name = "ultima_batalha_id")
	private Long ultimaBatalhaId;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private Instant criadoEm;

	public Tropa(Long vilaId, Long quartelId, String nome) {
		this.vilaId = vilaId;
		this.quartelId = quartelId;
		this.nome = nome;
	}

	@PrePersist
	void aoCriar() {
		criadoEm = Instant.now();
	}

}
