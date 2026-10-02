package com.example.loginbase.jogo.cidadao;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.item.BonusEquipamentoService;

/** Vida máxima = 30 + 5 x VIT total + 3 x G (PE efetivo de Guerreiro) + soma de VIDA dos itens. */
@Component
public class VidaMaximaCalculadora {

	private final CalculadoraPeEfetivo calculadoraPeEfetivo;
	private final BonusEquipamentoService bonusEquipamentoService;
	private final CidadaoProfissaoRepository cidadaoProfissaoRepository;

	public VidaMaximaCalculadora(CalculadoraPeEfetivo calculadoraPeEfetivo,
			BonusEquipamentoService bonusEquipamentoService, CidadaoProfissaoRepository cidadaoProfissaoRepository) {
		this.calculadoraPeEfetivo = calculadoraPeEfetivo;
		this.bonusEquipamentoService = bonusEquipamentoService;
		this.cidadaoProfissaoRepository = cidadaoProfissaoRepository;
	}

	public int calcular(Cidadao cidadao) {
		int peBase = cidadao.getId() == null ? 0
				: cidadaoProfissaoRepository.findByCidadaoIdAndProfissao(cidadao.getId(), Profissao.GUERREIRO)
						.map(CidadaoProfissao::getPontosBase).orElse(0);
		int g = calculadoraPeEfetivo.calcular(cidadao, Profissao.GUERREIRO, peBase);
		int vit = calculadoraPeEfetivo.caracteristicaTotal(cidadao, Caracteristica.VIT);
		int vida = cidadao.getId() == null ? 0 : bonusEquipamentoService.vidaItens(cidadao.getId());
		return 30 + 5 * vit + 3 * g + vida;
	}

}
