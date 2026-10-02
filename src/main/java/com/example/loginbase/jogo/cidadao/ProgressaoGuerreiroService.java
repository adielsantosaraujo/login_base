package com.example.loginbase.jogo.cidadao;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Progressão de Guerreiro por XP: a cada 10 XP acumulados converte em +1 PE base de GUERREIRO.
 * Ao converter, 10 XP são SUBTRAÍDOS (o resto é preservado: 9,5 + 1,5 = 11 vira PE+1 e XP 1,0).
 * Reutilizável pela Fase 5 (XP de vitória em masmorra). Idade não é checada: XP vira PE em qualquer idade.
 */
@Service
@RequiredArgsConstructor
public class ProgressaoGuerreiroService {

	static final BigDecimal XP_POR_PE = BigDecimal.TEN;

	private final CidadaoProfissaoRepository profissaoRepository;

	/**
	 * Soma {@code xp} ao cidadão e converte cada 10 XP em +1 PE base de Guerreiro (cria a linha de
	 * {@code cidadao_profissao} se faltar). Persiste profissão; o cidadão deve estar gerenciado/salvo pelo chamador.
	 *
	 * @return quantos PE foram ganhos
	 */
	@Transactional
	public int adicionarXp(Cidadao cidadao, BigDecimal xp) {
		if (xp == null || xp.signum() <= 0) {
			return 0;
		}
		BigDecimal total = cidadao.getXpGuerreiro().add(xp);
		int ganhos = 0;
		while (total.compareTo(XP_POR_PE) >= 0) {
			total = total.subtract(XP_POR_PE);
			ganhos++;
		}
		cidadao.setXpGuerreiro(total);
		if (ganhos > 0) {
			CidadaoProfissao prof = profissaoRepository
					.findByCidadaoIdAndProfissao(cidadao.getId(), Profissao.GUERREIRO)
					.orElseGet(() -> new CidadaoProfissao(cidadao.getId(), Profissao.GUERREIRO, 0));
			prof.setPontosBase(prof.getPontosBase() + ganhos);
			profissaoRepository.save(prof);
		}
		return ganhos;
	}

}
