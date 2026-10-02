package com.example.loginbase.jogo.batalha;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.cidadao.Caracteristica;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CalculadoraPeEfetivo;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.item.BonusEquipamentoService;
import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.item.catalogo.Alcance;
import com.example.loginbase.jogo.item.catalogo.ArmaCatalogo;
import com.example.loginbase.jogo.item.catalogo.ArmaduraCatalogo;

/** Monta o {@link Combatente} de um cidadão a partir de suas características e itens equipados. */
@Component
public class FabricaCombatente {

	private final CalculadoraPeEfetivo calculadoraPeEfetivo;
	private final BonusEquipamentoService bonusEquipamentoService;
	private final CidadaoProfissaoRepository cidadaoProfissaoRepository;
	private final ItemRepository itemRepository;
	private final CombateAtributos combateAtributos;

	public FabricaCombatente(CalculadoraPeEfetivo calculadoraPeEfetivo,
			BonusEquipamentoService bonusEquipamentoService, CidadaoProfissaoRepository cidadaoProfissaoRepository,
			ItemRepository itemRepository, CombateAtributos combateAtributos) {
		this.calculadoraPeEfetivo = calculadoraPeEfetivo;
		this.bonusEquipamentoService = bonusEquipamentoService;
		this.cidadaoProfissaoRepository = cidadaoProfissaoRepository;
		this.itemRepository = itemRepository;
		this.combateAtributos = combateAtributos;
	}

	public Combatente criar(Cidadao cidadao, LinhaCombate linha) {
		Long id = cidadao.getId();
		int peBase = cidadaoProfissaoRepository.findByCidadaoIdAndProfissao(id, Profissao.GUERREIRO)
				.map(CidadaoProfissao::getPontosBase).orElse(0);
		int g = calculadoraPeEfetivo.calcular(cidadao, Profissao.GUERREIRO, peBase);
		int vit = calculadoraPeEfetivo.caracteristicaTotal(cidadao, Caracteristica.VIT);
		int forca = calculadoraPeEfetivo.caracteristicaTotal(cidadao, Caracteristica.FOR);
		int vel = calculadoraPeEfetivo.caracteristicaTotal(cidadao, Caracteristica.VEL);
		int inteligencia = calculadoraPeEfetivo.caracteristicaTotal(cidadao, Caracteristica.INT);

		ArmaCatalogo arma = null;
		int nivelArma = 0;
		double defesaPecas = 0;
		int iniArmaduras = 0;
		List<Item> itens = itemRepository.findByCidadaoId(id).stream().filter(i -> i.getSlot() != null).toList();
		for (Item item : itens) {
			if (item.getSlot() == SlotEquipamento.ARMA) {
				arma = ArmaCatalogo.de(item.getSubtipo()).orElse(null);
				nivelArma = item.getNivel();
			} else {
				var armadura = ArmaduraCatalogo.de(item.getSubtipo());
				if (armadura.isPresent()) {
					defesaPecas += armadura.get().defesa(item.getNivel());
					iniArmaduras += armadura.get().iniciativaExtra();
				}
			}
		}

		EntradaCombate entrada = new EntradaCombate(vit, forca, vel, inteligencia, g,
				bonusEquipamentoService.vidaItens(id), arma, nivelArma, defesaPecas, iniArmaduras,
				bonusEquipamentoService.somaBonus(id, CodigoBonus.ATK),
				bonusEquipamentoService.somaBonus(id, CodigoBonus.DEF),
				bonusEquipamentoService.somaBonus(id, CodigoBonus.INI),
				bonusEquipamentoService.somaBonus(id, CodigoBonus.CRIT));
		AtributosCombate a = combateAtributos.calcular(entrada);

		Alcance alcance = arma == null ? Alcance.CORPO_A_CORPO_FRENTE : arma.alcance();
		return new Combatente(id, LadoCombate.TROPA, cidadao.getNome(), linha, a.pvMax(), a.ataque(), a.defesa(),
				a.iniciativaBase(), a.criticoPp(), vel, alcance, arma == ArmaCatalogo.BESTA);
	}
}
