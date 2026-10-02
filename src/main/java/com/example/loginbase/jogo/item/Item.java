package com.example.loginbase.jogo.item;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vila_id", nullable = false, updatable = false)
	private Long vilaId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ItemCategoria categoria;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ItemSubtipo subtipo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Qualidade qualidade;

	@Column(nullable = false)
	private int nivel;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private List<BonusItem> bonus = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "atributo_escolhido", length = 3)
	private CodigoBonus atributoEscolhido;

	@Column(name = "cidadao_id")
	private Long cidadaoId;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private SlotEquipamento slot;

	@Column(name = "em_aprimoramento", nullable = false)
	private boolean emAprimoramento;

	@Column(name = "criado_em", nullable = false, updatable = false, insertable = false)
	private Instant criadoEm;

	public Item(Long vilaId, ItemSubtipo subtipo, Qualidade qualidade, int nivel, List<BonusItem> bonus) {
		this.vilaId = vilaId;
		this.subtipo = subtipo;
		this.categoria = subtipo.getCategoria();
		this.qualidade = qualidade;
		this.nivel = nivel;
		this.bonus = bonus == null ? new ArrayList<>() : new ArrayList<>(bonus);
	}
}
