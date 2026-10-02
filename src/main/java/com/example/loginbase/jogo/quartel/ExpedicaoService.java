package com.example.loginbase.jogo.quartel;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.batalha.FonteMasmorras;
import com.example.loginbase.jogo.batalha.MasmorraAlvo;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.AlimentacaoService;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

import lombok.RequiredArgsConstructor;

/** Envio de tropas em expedição (expedicoes.md): destinos, custo de viagem e débito de comida. */
@Service
@RequiredArgsConstructor
public class ExpedicaoService {

	/** Comida por membro por turno de viagem (ida e volta: 2 x turnos). */
	static final int COMIDA_POR_MEMBRO_POR_TURNO = 2;

	private final TropaRepository tropaRepository;
	private final CidadaoRepository cidadaoRepository;
	private final RegiaoRepository regiaoRepository;
	private final FonteMasmorras fonteMasmorras;
	private final AlimentacaoService alimentacaoService;
	private final JogoTurnoRepository turnoRepository;
	private final RegistroEventoTurnoService registro;
	private final TropaService tropaService;

	@Transactional(readOnly = true)
	public List<DestinoExpedicaoDTO> destinos(Vila vila, Long tropaId) {
		Tropa tropa = tropaDaVila(vila, tropaId);
		long membros = cidadaoRepository.countByTropaId(tropa.getId());
		var disponivel = alimentacaoService.alimentosDisponiveis(vila);
		return fonteMasmorras.listarAtivas(vila.getId()).stream()
				.sorted(Comparator.comparingLong(MasmorraAlvo::id))
				.map(m -> {
					int turnos = turnosViagem(vila, m.regiaoIndice());
					return new DestinoExpedicaoDTO(m.id(), m.regiaoIndice(), m.nivel(), turnos,
							(int) (membros * COMIDA_POR_MEMBRO_POR_TURNO * turnos), disponivel);
				}).toList();
	}

	@Transactional
	public TropaDTO enviar(Vila vila, Long tropaId, ExpedicaoRequest req) {
		Tropa tropa = tropaDaVila(vila, tropaId);
		if (tropa.getEstado() != EstadoTropa.AQUARTELADA) {
			throw erro("Tropa já está em expedição");
		}
		List<Cidadao> membros = cidadaoRepository.findByTropaId(tropa.getId());
		if (membros.isEmpty()) {
			throw erro("Tropa sem membros");
		}
		if (membros.stream().anyMatch(c -> c.getEstado() == EstadoCidadao.FERIDO)) {
			throw erro("Tropa com membro ferido não pode partir em expedição");
		}
		if (req == null || req.masmorraId() == null) {
			throw erro("Informe a masmorra");
		}
		MasmorraAlvo alvo = fonteMasmorras.buscarAtiva(vila.getId(), req.masmorraId())
				.orElseThrow(() -> erro("Masmorra não encontrada ou inativa"));
		int turnos = turnosViagem(vila, alvo.regiaoIndice());
		int comida = membros.size() * COMIDA_POR_MEMBRO_POR_TURNO * turnos;
		alimentacaoService.debitarAlimentos(vila, comida);

		tropa.setEstado(EstadoTropa.EM_VIAGEM_IDA);
		tropa.setMasmorraId(alvo.id());
		tropa.setRegiaoDestino(alvo.regiaoIndice());
		tropa.setTurnosViagem(turnos);
		tropa.setTurnosRestantes(turnos);
		tropaRepository.saveAndFlush(tropa);

		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("tropaId", tropa.getId());
		dados.put("tropaNome", tropa.getNome());
		dados.put("masmorraId", alvo.id());
		dados.put("regiao", alvo.regiaoIndice());
		dados.put("turnosViagem", turnos);
		dados.put("membros", membros.size());
		dados.put("comida", comida);
		registro.registrar(vila, turnoRepository.maiorNumero(), TipoEventoTurno.EXPEDICAO_PARTIU,
				"A tropa " + tropa.getNome() + " partiu em expedição (" + turnos + " turno(s) de viagem).", dados);
		return tropaService.tropa(vila, tropa.getId());
	}

	/** max(1; menor distância de Manhattan entre a região da masmorra e qualquer região possuída). */
	int turnosViagem(Vila vila, int regiaoMasmorra) {
		int menor = regiaoRepository.findAllByVilaId(vila.getId()).stream().filter(Regiao::isPossuida)
				.mapToInt(r -> Math.abs(GradeRegioes.linha(r.getIndice()) - GradeRegioes.linha(regiaoMasmorra))
						+ Math.abs(GradeRegioes.coluna(r.getIndice()) - GradeRegioes.coluna(regiaoMasmorra)))
				.min().orElse(1);
		return Math.max(1, menor);
	}

	private Tropa tropaDaVila(Vila vila, Long id) {
		return tropaRepository.findByIdAndVilaId(id, vila.getId())
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Tropa não encontrada"));
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}
}
