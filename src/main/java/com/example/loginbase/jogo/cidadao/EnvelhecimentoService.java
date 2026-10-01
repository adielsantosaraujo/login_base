package com.example.loginbase.jogo.cidadao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/** Passo 10 do turno: +1 mês, aniversários (pontos de crescimento) e teste de morte por idade. */
@Service
public class EnvelhecimentoService {

	public static final int IDADE_MAX_CRESCIMENTO = 18;
	public static final int IDADE_INICIO_RISCO = 50;
	public static final int IDADE_MORTE_CERTA = 90;

	private final CidadaoRepository cidadaoRepository;
	private final MorteService morteService;
	private final RegistroEventoTurnoService registro;
	private final RandomGenerator random;

	@Autowired
	public EnvelhecimentoService(CidadaoRepository cidadaoRepository, MorteService morteService,
			RegistroEventoTurnoService registro) {
		this(cidadaoRepository, morteService, registro, RandomGenerator.getDefault());
	}

	public EnvelhecimentoService(CidadaoRepository cidadaoRepository, MorteService morteService,
			RegistroEventoTurnoService registro, RandomGenerator random) {
		this.cidadaoRepository = cidadaoRepository;
		this.morteService = morteService;
		this.registro = registro;
		this.random = random;
	}

	@Transactional
	public void processar(Vila vila, int turno) {
		for (Cidadao c : cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId())) {
			c.setIdadeMeses(c.getIdadeMeses() + 1);
			if (c.getIdadeMeses() % 12 != 0) {
				cidadaoRepository.save(c);
				continue;
			}
			int anos = c.getIdadeAnos();
			processarAniversario(vila, c, anos, turno);
			cidadaoRepository.save(c);
			double chance = calcularChanceMorte(anos, c.getVit());
			if (chance > 0 && (chance >= 1.0 || random.nextDouble() < chance)) {
				morteService.morrer(vila, c, "IDADE", turno);
			}
		}
	}

	void processarAniversario(Vila vila, Cidadao c, int anos, int turno) {
		if (anos >= 1 && anos <= IDADE_MAX_CRESCIMENTO) {
			c.setPontosCarPendentes(c.getPontosCarPendentes() + 1);
			if (anos % 2 == 0) {
				c.setPontosProfPendentes(c.getPontosProfPendentes() + 1);
			}
		}
		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("cidadaoId", c.getId());
		dados.put("nome", c.getNome());
		dados.put("idadeAnos", anos);
		dados.put("pontosCarPendentes", c.getPontosCarPendentes());
		dados.put("pontosProfPendentes", c.getPontosProfPendentes());
		registro.registrar(vila, turno, TipoEventoTurno.ANIVERSARIO,
				c.getNome() + " completou " + anos + " ano(s).", dados);
	}

	public static double calcularChanceMorte(int anos, int vit) {
		if (anos >= IDADE_MORTE_CERTA) {
			return 1.0;
		}
		if (anos >= IDADE_INICIO_RISCO) {
			return Math.max(0.0, 0.01 * (anos - 49) - 0.002 * vit);
		}
		return 0.0;
	}

}
