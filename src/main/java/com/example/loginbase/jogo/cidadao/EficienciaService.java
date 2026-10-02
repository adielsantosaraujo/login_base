package com.example.loginbase.jogo.cidadao;

import java.util.Comparator;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.loginbase.jogo.item.BonusEquipamentoService;
import com.example.loginbase.jogo.modelo.Vila;

/**
 * Eficiência do trabalhador (bíblia 5.3):
 * {@code (0,5 + 0,1 × PE efetivo) × modificadores × multiplicadores}, com limite 3,0 após a base
 * e novamente no resultado final.
 *
 * <p>API para os demais serviços (obras, produção, ouro, mercado):
 * {@code double eficiencia(Cidadao, Profissao, double multiplicadorNivel, Vila)}.
 * O multiplicadorNivel (ex.: 1,0 / 1,1 ...) entra junto dos multiplicadores (líder, PROD%).
 */
@Service
public class EficienciaService {

	static final double LIMITE = 3.0;
	static final int IDADE_ADULTO = 18;

	private final CalculadoraPeEfetivo calculadoraPeEfetivo;
	private final CidadaoProfissaoRepository cidadaoProfissaoRepository;
	private final CidadaoRepository cidadaoRepository;
	private final BonusEquipamentoService bonusEquipamentoService;

	public EficienciaService(CalculadoraPeEfetivo calculadoraPeEfetivo,
			CidadaoProfissaoRepository cidadaoProfissaoRepository, CidadaoRepository cidadaoRepository,
			BonusEquipamentoService bonusEquipamentoService) {
		this.bonusEquipamentoService = bonusEquipamentoService;
		this.calculadoraPeEfetivo = calculadoraPeEfetivo;
		this.cidadaoProfissaoRepository = cidadaoProfissaoRepository;
		this.cidadaoRepository = cidadaoRepository;
	}

	/** Eficiência no intervalo [0, 3,0] (sem multiplicador de nível: 1,0). */
	public double eficiencia(Cidadao cidadao, Profissao profissao, Vila vila) {
		return eficiencia(cidadao, profissao, 1.0, vila);
	}

	public double eficiencia(Cidadao cidadao, Profissao profissao, double multiplicadorNivel, Vila vila) {
		int peBase = cidadaoProfissaoRepository.findByCidadaoIdAndProfissao(cidadao.getId(), profissao)
				.map(CidadaoProfissao::getPontosBase).orElse(0);
		int peEfetivo = calculadoraPeEfetivo.calcular(cidadao, profissao, peBase);
		boolean temPeItens = calculadoraPeEfetivo.peItens(cidadao, profissao) > 0;
		double prodItens = cidadao.getId() == null ? 0.0 : bonusEquipamentoService.prodItens(cidadao.getId()) / 100.0;
		return calcular(peBase, peEfetivo, cidadao.getIdadeAnos(), cidadao.getFamintoTurnos() > 0,
				vila != null && vila.isBemAlimentada(), bonusLider(vila), prodItens, multiplicadorNivel, temPeItens);
	}

	static double calcular(int peBase, int peEfetivo, int idadeAnos, boolean faminto, boolean bemAlimentada,
			double bonusLider, double prodItens, double multiplicadorNivel) {
		return calcular(peBase, peEfetivo, idadeAnos, faminto, bemAlimentada, bonusLider, prodItens,
				multiplicadorNivel, false);
	}

	/** Fórmula pura. Bônus de líder e PROD% em fração (0,05 = 5%). */
	static double calcular(int peBase, int peEfetivo, int idadeAnos, boolean faminto, boolean bemAlimentada,
			double bonusLider, double prodItens, double multiplicadorNivel, boolean temPeItens) {
		// Sem PE base na profissão e sem PE de itens: rende 0,5.
		double base = peBase == 0 && !temPeItens ? 0.5 : 0.5 + 0.1 * peEfetivo;
		double r = Math.min(LIMITE, base);
		if (idadeAnos >= 14 && idadeAnos <= 17) {
			r *= 0.5;
		}
		if (faminto) {
			r *= 0.5;
		}
		if (bemAlimentada) {
			r *= 1.10;
		}
		r *= (1 + bonusLider) * (1 + prodItens) * multiplicadorNivel;
		return Math.max(0.0, Math.min(LIMITE, r));
	}

	/** Bônus do líder (fração): floor(CAR ÷ 2)%, máx. 10%. Líder = adulto vivo mais velho da família líder. */
	public double bonusLider(Vila vila) {
		if (vila == null || vila.getFamiliaLiderId() == null) {
			return 0.0;
		}
		Optional<Cidadao> lider = cidadaoRepository.findByFamiliaIdAndVivoTrue(vila.getFamiliaLiderId()).stream()
				.filter(c -> c.getIdadeAnos() >= IDADE_ADULTO)
				.max(Comparator.comparingInt(Cidadao::getIdadeMeses).thenComparing(Cidadao::getId,
						Comparator.nullsFirst(Comparator.reverseOrder())));
		return lider.map(c -> Math.min(10, c.getCar() / 2) / 100.0).orElse(0.0);
	}

}
