package com.example.loginbase.jogo.batalha;

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

/** Batalha de expedição gravada (log completo para o replay). */
@Entity
@Table(name = "batalha")
@Getter
@Setter
@NoArgsConstructor
public class Batalha {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	/** FK com on delete set null: a tropa pode ser desfeita depois. */
	@Column(name = "tropa_id")
	private Long tropaId;

	@Column(name = "tropa_nome", nullable = false, length = 100)
	private String tropaNome;

	@Column(name = "masmorra_id")
	private Long masmorraId;

	@Column(name = "masmorra_nivel", nullable = false)
	private int masmorraNivel;

	@Column(name = "regiao_indice", nullable = false)
	private int regiaoIndice;

	@Column(nullable = false)
	private int turno;

	@Column(nullable = false)
	private long semente;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ResultadoCombate resultado;

	@Column(nullable = false)
	private int rodadas;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private LogBatalha log;

	/**
	 * Recompensas entregues na vitória (null na derrota): {ouro, recursos{RECURSO:qtd}, item{itemId, subtipo, categoria,
	 * nivel, qualidade, bonus[{codigo,valor}], atributoEscolhido} | null, xpPorGuerreiro, guerreirosXp[ids],
	 * pedras[{pedraId, qualidade, bonus[{codigo,magnitude,valor}]}]}.
	 */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> recompensas;

	@Column(name = "criado_em", nullable = false, updatable = false, insertable = false)
	private Instant criadoEm;

}
