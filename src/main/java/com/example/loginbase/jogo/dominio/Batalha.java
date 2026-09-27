package com.example.loginbase.jogo.dominio;

import java.time.Instant;

import com.example.loginbase.auditoria.EntidadeAuditavel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Batalha de uma vila em uma masmorra. {@code estado} e {@code log} guardam,
 * em JSON/texto, o estado tático e o log de turnos; {@code loot} guarda o
 * JSON do loot quando a batalha termina em vitória. Controle otimista via
 * {@link #version}, já que o motor de combate (task 5.1) processa turnos
 * concorrentemente com leituras.
 *
 * <p>
 * {@code estado} e {@code loot} são, por ora, JSON serializado em
 * {@code String} (ver nota em {@link JsonConverter}): os tipos
 * {@code EstadoBatalha} e {@code Loot} ainda não existem neste momento —
 * pertencem às tasks 5.1 (motor de combate) e 5.2 (loot) da mesma change.
 */
@Entity
@Table(name = "jogo_batalhas")
@Getter
@Setter
@NoArgsConstructor
public class Batalha extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false)
	private Long vilaId;

	@Column(name = "masmorra_nivel", nullable = false)
	private int masmorraNivel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusBatalha status;

	@Column(nullable = false)
	private int turno;

	@Column(nullable = false, columnDefinition = "text")
	private String estado;

	@Column(nullable = false, columnDefinition = "text")
	private String log;

	@Column(columnDefinition = "text")
	private String loot;

	@Column(name = "iniciada_em", nullable = false)
	private Instant iniciadaEm;

	@Column(name = "finalizada_em")
	private Instant finalizadaEm;

	@Version
	@Column(nullable = false)
	private long version;

}
