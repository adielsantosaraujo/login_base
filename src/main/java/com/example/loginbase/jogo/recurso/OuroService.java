package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores.Trabalhador;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.BonusTerrenoService;
import com.example.loginbase.jogo.servico.GrupoBonusVila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Passo 2 do turno: ouro passivo. Imposto (0,5 Ouro por cidadão vivo com 18 anos ou mais) e receita da
 * Estalagem (4 Ouro por Refeição servida; capacidade = floor(5 x soma das eficiências), já com o
 * multiplicador do nível embutido na eficiência; só Refeições inteiras são servidas).
 */
@Service
public class OuroService {

	public static final int IDADE_IMPOSTO_ANOS = 18;
	static final BigDecimal IMPOSTO_POR_ADULTO = new BigDecimal("0.5");
	static final BigDecimal OURO_POR_REFEICAO = BigDecimal.valueOf(4);
	private static final BigDecimal CEM = BigDecimal.valueOf(100);
	static final int REFEICOES_POR_EFICIENCIA = 5;

	private final CidadaoRepository cidadaoRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final ConsultaTrabalhadores consultaTrabalhadores;
	private final EstoqueService estoqueService;
	private final RegistroEventoTurnoService registro;
	private final BonusTerrenoService bonusTerrenoService;

	public OuroService(CidadaoRepository cidadaoRepository, ConstrucaoRepository construcaoRepository,
			ConsultaTrabalhadores consultaTrabalhadores, EstoqueService estoqueService,
			RegistroEventoTurnoService registro, BonusTerrenoService bonusTerrenoService) {
		this.cidadaoRepository = cidadaoRepository;
		this.construcaoRepository = construcaoRepository;
		this.consultaTrabalhadores = consultaTrabalhadores;
		this.estoqueService = estoqueService;
		this.registro = registro;
		this.bonusTerrenoService = bonusTerrenoService;
	}

	@Transactional
	public void processarOuroPassivo(Vila vila, int turno) {
		BigDecimal media = bonusTerrenoService.media(vila.getId(), GrupoBonusVila.COMERCIO);
		BigDecimal fator = BigDecimal.ONE.add(media.divide(CEM, 4, RoundingMode.HALF_UP));
		cobrarImposto(vila, turno, media, fator);
		receitaEstalagens(vila, turno, media, fator);
	}

	private void cobrarImposto(Vila vila, int turno, BigDecimal media, BigDecimal fator) {
		long adultos = cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId()).stream()
				.filter(c -> c.getIdadeAnos() >= IDADE_IMPOSTO_ANOS).count();
		BigDecimal ouro = IMPOSTO_POR_ADULTO.multiply(BigDecimal.valueOf(adultos)).multiply(fator)
				.setScale(2, RoundingMode.HALF_UP);
		if (ouro.signum() > 0) {
			estoqueService.creditar(vila, Recurso.OURO, ouro);
		}
		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("adultos", adultos);
		dados.put("ouro", ouro);
		if (media.signum() > 0) {
			dados.put("bonusTerreno", media.setScale(2, RoundingMode.HALF_UP));
		}
		registro.registrar(vila, turno, TipoEventoTurno.IMPOSTO_COBRADO,
				"Imposto: " + ouro.setScale(2, RoundingMode.HALF_UP).toPlainString() + " Ouro de " + adultos
						+ " adulto(s).", dados);
	}

	private void receitaEstalagens(Vila vila, int turno, BigDecimal media, BigDecimal fator) {
		List<Construcao> estalagens = construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.ATIVA)
				.stream().filter(c -> c.getTipo() == TipoConstrucao.ESTALAGEM)
				.sorted(java.util.Comparator.comparing(Construcao::getId)).toList();
		for (Construcao estalagem : estalagens) {
			double soma = 0;
			for (Trabalhador t : consultaTrabalhadores.trabalhadores(estalagem, vila)) {
				if (t.profissao() == Profissao.COZINHEIRO || t.profissao() == Profissao.COMERCIANTE) {
					soma += t.eficiencia();
				}
			}
			// arredonda ruído de ponto flutuante antes do floor
			long capacidade = BigDecimal.valueOf(soma).multiply(BigDecimal.valueOf(REFEICOES_POR_EFICIENCIA))
					.setScale(6, RoundingMode.HALF_UP).setScale(0, RoundingMode.FLOOR).longValueExact();
			BigDecimal emEstoque = estoqueService.quantidade(vila, Recurso.REFEICAO).setScale(0, RoundingMode.FLOOR);
			long servidas = Math.min(capacidade, emEstoque.longValueExact());
			BigDecimal ouro = OURO_POR_REFEICAO.multiply(BigDecimal.valueOf(servidas)).multiply(fator)
					.setScale(2, RoundingMode.HALF_UP);
			if (servidas > 0) {
				estoqueService.debitar(vila, Map.of(Recurso.REFEICAO, BigDecimal.valueOf(servidas)));
				estoqueService.creditar(vila, Recurso.OURO, ouro);
			}
			Map<String, Object> dados = new LinkedHashMap<>();
			dados.put("construcaoId", estalagem.getId());
			dados.put("capacidade", capacidade);
			dados.put("refeicoesServidas", servidas);
			dados.put("ouro", ouro);
			if (media.signum() > 0) {
				dados.put("bonusTerreno", media.setScale(2, RoundingMode.HALF_UP));
			}
			registro.registrar(vila, turno, TipoEventoTurno.ESTALAGEM_RECEITA,
					"Estalagem serviu " + servidas + " refeição(ões) e gerou "
							+ ouro.setScale(2, RoundingMode.HALF_UP).toPlainString() + " Ouro.", dados);
		}
	}

}
