package com.example.loginbase.jogo.servico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.dto.AnexacaoDTO;
import com.example.loginbase.jogo.dto.AnexacaoDTO.RegiaoAnexadaDTO;
import com.example.loginbase.jogo.dto.CustoAnexacaoDTO;
import com.example.loginbase.jogo.excecao.RegiaoComMasmorraException;
import com.example.loginbase.jogo.excecao.RegiaoJaPossuidaException;
import com.example.loginbase.jogo.excecao.RegiaoNaoAdjacenteException;
import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Regra de anexação de novas regiões à vila (regioes.md): adjacência ortogonal, sem masmorra ativa e custo
 * crescente com o número de regiões possuídas (k).
 */
@Service
public class AnexacaoService {

	private static final BigDecimal OURO_BASE = BigDecimal.valueOf(150);
	private static final BigDecimal FATOR_OURO = new BigDecimal("1.35");
	private static final int K_MINIMO = 3;

	private final RegiaoRepository regiaoRepository;
	private final ConsultaMasmorras consultaMasmorras;
	private final EstoqueService estoqueService;
	private final RegistroEventoTurnoService registroEventoTurno;
	private final JogoTurnoRepository turnoRepository;
	private final TerrenoRegiaoService terrenoRegiaoService;

	public AnexacaoService(RegiaoRepository regiaoRepository,
			ConsultaMasmorras consultaMasmorras, EstoqueService estoqueService,
			RegistroEventoTurnoService registroEventoTurno, JogoTurnoRepository turnoRepository,
			TerrenoRegiaoService terrenoRegiaoService) {
		this.regiaoRepository = regiaoRepository;
		this.consultaMasmorras = consultaMasmorras;
		this.estoqueService = estoqueService;
		this.registroEventoTurno = registroEventoTurno;
		this.turnoRepository = turnoRepository;
		this.terrenoRegiaoService = terrenoRegiaoService;
	}

	/** Custo para o k informado (k &lt; 3 é tratado como 3); arredondamento meio para cima. */
	public static CustoAnexacaoDTO custoParaK(int k) {
		int kk = Math.max(k, K_MINIMO);
		long ouro = OURO_BASE.multiply(FATOR_OURO.pow(kk - K_MINIMO)).setScale(0, RoundingMode.HALF_UP).longValueExact();
		long materiais = 50L * (kk - 2);
		return new CustoAnexacaoDTO(ouro, materiais, materiais);
	}

	/** Custo da próxima anexação da vila (k = regiões possuídas hoje). */
	@Transactional(readOnly = true)
	public CustoAnexacaoDTO calcularCustoAnexacao(Vila vila) {
		return custoParaK(contarPossuidas(vila));
	}

	/** Se o índice é adjacente (ortogonal) a alguma região possuída da vila. */
	@Transactional(readOnly = true)
	public boolean validarAdjacencia(Vila vila, int indiceRegiao) {
		return adjacenteAPossuida(regiaoRepository.findAllByVilaId(vila.getId()), indiceRegiao);
	}

	@Transactional
	public AnexacaoDTO anexarRegiao(Vila vila, int indiceRegiao) {
		if (indiceRegiao < 1 || indiceRegiao > GradeRegioes.TOTAL) {
			throw new JogoException(HttpStatus.NOT_FOUND, "Região inexistente: " + indiceRegiao);
		}
		List<Regiao> regioes = regiaoRepository.findAllByVilaId(vila.getId());
		if (regioes.stream().anyMatch(r -> r.getIndice() == indiceRegiao && r.isPossuida())) {
			throw new RegiaoJaPossuidaException("Região já possuída");
		}
		TipoRegiao tipo = regioes.stream().filter(r -> r.getIndice() == indiceRegiao).findFirst()
				.map(Regiao::getTipo).orElse(null);
		if (tipo == null) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Região sem tipo");
		}
		if (!adjacenteAPossuida(regioes, indiceRegiao)) {
			throw new RegiaoNaoAdjacenteException("Região deve ser adjacente a uma já possuída");
		}
		if (consultaMasmorras.nivelMasmorraAtiva(vila, indiceRegiao).isPresent()) {
			throw new RegiaoComMasmorraException("Não é possível anexar região com masmorra ativa");
		}

		int k = (int) regioes.stream().filter(Regiao::isPossuida).count();
		CustoAnexacaoDTO custo = custoParaK(k);
		Map<Recurso, BigDecimal> debito = new EnumMap<>(Recurso.class);
		debito.put(Recurso.OURO, BigDecimal.valueOf(custo.ouro()));
		debito.put(Recurso.MADEIRA, BigDecimal.valueOf(custo.madeira()));
		debito.put(Recurso.PEDRA, BigDecimal.valueOf(custo.pedra()));
		estoqueService.debitar(vila, debito);

		Regiao regiao = regioes.stream().filter(r -> r.getIndice() == indiceRegiao).findFirst()
				.orElseGet(() -> new Regiao(vila.getId(), indiceRegiao));
		regiao.setPossuida(true);
		regiao = regiaoRepository.saveAndFlush(regiao);

		terrenoRegiaoService.gerarLadrilhosSeAusentes(vila.getSemente(), regiao);

		int turno = turnoRepository.maiorNumero();
		registroEventoTurno.registrar(vila, turno, TipoEventoTurno.REGIAO_ANEXADA,
				"Região " + indiceRegiao + " anexada (" + tipo + ")",
				Map.of("regiao", indiceRegiao, "tipo", tipo.name(), "ouro", custo.ouro(), "madeira", custo.madeira(),
						"pedra", custo.pedra()));

		return new AnexacaoDTO(new RegiaoAnexadaDTO(indiceRegiao, tipo, true,
				terrenoRegiaoService.terrenosDaRegiao(regiao.getId())), estoqueService.listar(vila), custo);
	}

	private int contarPossuidas(Vila vila) {
		return (int) regiaoRepository.findAllByVilaId(vila.getId()).stream().filter(Regiao::isPossuida).count();
	}

	private boolean adjacenteAPossuida(List<Regiao> regioes, int indiceRegiao) {
		return regioes.stream().filter(Regiao::isPossuida)
				.anyMatch(r -> GradeRegioes.adjacente(r.getIndice(), indiceRegiao));
	}

}
