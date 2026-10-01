package com.example.loginbase.jogo.cidadao;

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
@Table(name = "cidadao")
@Getter
@Setter
@NoArgsConstructor
public class Cidadao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Column(name = "familia_id", nullable = false)
	private Long familiaId;

	@Column(nullable = false, length = 100)
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 1)
	private Sexo sexo;

	@Column(name = "idade_meses", nullable = false)
	private int idadeMeses;

	@Column(nullable = false)
	private int vit;

	@Column(name = "forca", nullable = false)
	private int forca;

	@Column(nullable = false)
	private int vel;

	@Column(name = "inteligencia", nullable = false)
	private int inteligencia;

	@Column(nullable = false)
	private int car;

	@Column(name = "pontos_car_pendentes", nullable = false)
	private int pontosCarPendentes;

	@Column(name = "pontos_prof_pendentes", nullable = false)
	private int pontosProfPendentes;

	@Column(name = "conjuge_id")
	private Long conjugeId;

	@Column(name = "pai_id")
	private Long paiId;

	@Column(name = "mae_id")
	private Long maeId;

	@Column(nullable = false)
	private boolean vivo = true;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoCidadao estado = EstadoCidadao.SAUDAVEL;

	@Column(name = "ferido_ate_turno")
	private Integer feridoAteTurno;

	@Column(name = "faminto_turnos", nullable = false)
	private int famintoTurnos;

	@Column(name = "gestacao_turnos")
	private Integer gestacaoTurnos;

	/** Turno do último parto da mãe (intervalo de 12 turnos entre partos). */
	@Column(name = "turno_ultimo_parto")
	private Integer turnoUltimoParto;

	@Column(name = "construcao_id")
	private Long construcaoId;

	/** Profissão exercida no prédio onde está alocado (null quando não alocado). */
	@Enumerated(EnumType.STRING)
	@Column(name = "profissao_trabalho", length = 30)
	private Profissao profissaoTrabalho;

	/** Sem FK por enquanto (tabela de tropas ainda não existe). */
	@Column(name = "tropa_id")
	private Long tropaId;

	@Column(name = "xp_guerreiro", nullable = false)
	private int xpGuerreiro;

	public Cidadao(Long vilaId, Long familiaId, String nome, Sexo sexo, int idadeMeses) {
		this.vilaId = vilaId;
		this.familiaId = familiaId;
		this.nome = nome;
		this.sexo = sexo;
		this.idadeMeses = idadeMeses;
	}

	public int getIdadeAnos() {
		return idadeMeses / 12;
	}

	public int valorCaracteristica(Caracteristica c) {
		return switch (c) {
			case VIT -> vit;
			case FOR -> forca;
			case VEL -> vel;
			case INT -> inteligencia;
			case CAR -> car;
		};
	}

}
