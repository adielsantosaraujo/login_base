package com.example.loginbase.jogo.quartel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.cidadao.ProgressaoGuerreiroService;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.BonusTerrenoService;
import com.example.loginbase.jogo.servico.GrupoBonusVila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

import lombok.RequiredArgsConstructor;

/** Etapa 7 do turno: treino. Membros de tropas aquarteladas ganham XP conforme o nível do quartel. */
@Service
@RequiredArgsConstructor
public class TreinamentoQuartelService {

	private final TropaRepository tropaRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final CidadaoRepository cidadaoRepository;
	private final ProgressaoGuerreiroService progressao;
	private final RegistroEventoTurnoService eventos;
	private final BonusTerrenoService bonusTerrenoService;

	/** XP por membro por turno: N1 0,5 / N2 1,0 / N3 1,5. */
	public static BigDecimal xpPorTurno(NivelConstrucao nivel) {
		return switch (nivel) {
			case N1 -> new BigDecimal("0.5");
			case N2 -> new BigDecimal("1.0");
			case N3 -> new BigDecimal("1.5");
		};
	}

	@Transactional
	public void processar(Vila vila, int turno) {
		BigDecimal fator = bonusTerrenoService.fator(vila.getId(), GrupoBonusVila.MILITAR);
		for (Tropa tropa : tropaRepository.findByVilaId(vila.getId())) {
			if (tropa.getEstado() != EstadoTropa.AQUARTELADA) {
				continue;
			}
			Construcao quartel = construcaoRepository.findById(tropa.getQuartelId()).orElse(null);
			if (quartel == null || quartel.getTipo() != TipoConstrucao.QUARTEL
					|| quartel.getEstado() != EstadoConstrucao.ATIVA || !temInstrutor(quartel)) {
				continue;
			}
			BigDecimal xp = xpPorTurno(quartel.getNivel()).multiply(fator).setScale(2, RoundingMode.HALF_UP);
			for (Cidadao membro : cidadaoRepository.findByTropaId(tropa.getId())) {
				if (!membro.isVivo() || membro.getEstado() != EstadoCidadao.SAUDAVEL) {
					continue;
				}
				int pe = progressao.adicionarXp(membro, xp);
				cidadaoRepository.save(membro);
				if (pe > 0) {
					eventos.registrar(vila, turno, TipoEventoTurno.TREINO_PE_GUERREIRO,
							membro.getNome() + " ganhou " + pe + " PE de Guerreiro por treinamento.",
							Map.of("cidadaoId", membro.getId(), "tropaId", tropa.getId(), "pe", pe));
				}
			}
		}
	}

	private boolean temInstrutor(Construcao quartel) {
		return cidadaoRepository.findByConstrucaoIdAndVivoTrue(quartel.getId()).stream()
				.anyMatch(c -> c.getProfissaoTrabalho() == Profissao.GUERREIRO
						&& c.getEstado() == EstadoCidadao.SAUDAVEL);
	}

}
