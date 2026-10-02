package com.example.loginbase.jogo.batalha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.cidadao.CalculadoraPeEfetivo;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.item.BonusEquipamentoService;
import com.example.loginbase.jogo.item.BonusItem;
import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.item.catalogo.Alcance;

class FabricaCombatenteTest {

	private final ItemRepository itens = mock(ItemRepository.class);
	private final CidadaoProfissaoRepository profs = mock(CidadaoProfissaoRepository.class);
	private final BonusEquipamentoService bonus = new BonusEquipamentoService(itens);
	private final FabricaCombatente fabrica = new FabricaCombatente(new CalculadoraPeEfetivo(bonus), bonus, profs,
			itens, new CombateAtributos());

	private Cidadao guerreiro() {
		Cidadao c = new Cidadao(1L, 1L, "Gui", Sexo.M, 480);
		c.setId(7L);
		c.setVit(5);
		c.setForca(6);
		c.setVel(5);
		when(profs.findByCidadaoIdAndProfissao(7L, Profissao.GUERREIRO))
				.thenReturn(Optional.of(new CidadaoProfissao(7L, Profissao.GUERREIRO, 5)));
		return c;
	}

	private Item item(ItemSubtipo st, int nivel, SlotEquipamento slot, List<BonusItem> b) {
		Item i = new Item(1L, st, Qualidade.SIMPLES, nivel, b);
		i.setCidadaoId(7L);
		i.setSlot(slot);
		return i;
	}

	@Test
	void exemplo104ComEspada() {
		Cidadao c = guerreiro();
		when(itens.findByCidadaoId(7L)).thenReturn(List.of(item(ItemSubtipo.ESPADA, 1, SlotEquipamento.ARMA, List.of())));
		Combatente k = fabrica.criar(c, LinhaCombate.FRENTE);
		assertThat(k.id()).isEqualTo(7L);
		assertThat(k.lado()).isEqualTo(LadoCombate.TROPA);
		assertThat(k.pvMax()).isEqualTo(79);
		assertThat(k.ataque()).isCloseTo(29, within(1e-9));
		assertThat(k.defesa()).isCloseTo(15, within(1e-9));
		assertThat(k.iniciativaBase()).isEqualTo(18);
		assertThat(k.alcance()).isEqualTo(Alcance.CORPO_A_CORPO_FRENTE);
		assertThat(k.ignoraDefesa25()).isFalse();
		assertThat(k.vel()).isEqualTo(5);
	}

	@Test
	void armaduraEBonusDeItens() {
		Cidadao c = guerreiro();
		when(itens.findByCidadaoId(7L)).thenReturn(List.of(
				item(ItemSubtipo.ESPADA, 1, SlotEquipamento.ARMA, List.of(new BonusItem(CodigoBonus.ATK, 3))),
				item(ItemSubtipo.PEITORAL, 1, SlotEquipamento.PEITORAL, List.of()),
				item(ItemSubtipo.SAPATO, 1, SlotEquipamento.SAPATO, List.of())));
		Combatente k = fabrica.criar(c, LinhaCombate.FRENTE);
		assertThat(k.defesa()).isCloseTo(10 + 5 + 8 + 2, within(1e-9));
		assertThat(k.ataque()).isCloseTo(29 * 1.03, within(1e-9));
		assertThat(k.iniciativaBase()).isEqualTo(19);
	}

	@Test
	void bestaIgnoraDefesaEAlcanceDistancia() {
		Cidadao c = guerreiro();
		when(itens.findByCidadaoId(7L)).thenReturn(List.of(item(ItemSubtipo.BESTA, 1, SlotEquipamento.ARMA, List.of())));
		Combatente k = fabrica.criar(c, LinhaCombate.RETAGUARDA);
		assertThat(k.alcance()).isEqualTo(Alcance.DISTANCIA);
		assertThat(k.ignoraDefesa25()).isTrue();
		assertThat(k.linha()).isEqualTo(LinhaCombate.RETAGUARDA);
		assertThat(k.iniciativaBase()).isEqualTo(10 + 8 - 4);
	}

	@Test
	void itemNaoEquipadoIgnorado() {
		Cidadao c = guerreiro();
		when(itens.findByCidadaoId(any())).thenReturn(List.of(item(ItemSubtipo.PEITORAL, 1, null, List.of())));
		assertThat(fabrica.criar(c, LinhaCombate.FRENTE).defesa()).isCloseTo(13, within(1e-9));
	}
}
