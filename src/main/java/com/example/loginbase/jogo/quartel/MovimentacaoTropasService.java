package com.example.loginbase.jogo.quartel;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.batalha.BatalhaExpedicaoService;
import com.example.loginbase.jogo.batalha.FonteMasmorras;
import com.example.loginbase.jogo.batalha.MasmorraAlvo;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

import lombok.RequiredArgsConstructor;

/**
 * Passo 8 do turno: movimentação das tropas. Ida decrementa; ao chegar a 0 busca a masmorra e, se ainda ativa,
 * batalha (senão volta sem lutar); em ambos os casos a tropa passa a EM_VIAGEM_VOLTA com
 * {@code turnosRestantes = turnosViagem}. Na volta, ao chegar a 0 fica AQUARTELADA.
 * Decisão: se todos os membros morreram na batalha, a tropa ainda volta (vazia) e fica AQUARTELADA, podendo
 * receber novos membros ou ser desfeita pelo jogador.
 */
@Service
@RequiredArgsConstructor
public class MovimentacaoTropasService {

	private final TropaRepository tropaRepository;
	private final CidadaoRepository cidadaoRepository;
	private final FonteMasmorras fonteMasmorras;
	private final BatalhaExpedicaoService batalhaService;
	private final RegistroEventoTurnoService registro;

	@Transactional
	public void processar(Vila vila, int turno) {
		var tropas = tropaRepository.findByVilaIdAndEstadoInOrderByIdAsc(vila.getId(),
				EnumSet.of(EstadoTropa.EM_VIAGEM_IDA, EstadoTropa.EM_VIAGEM_VOLTA));
		for (Tropa t : tropas) {
			int restantes = (t.getTurnosRestantes() == null ? 0 : t.getTurnosRestantes()) - 1;
			if (t.getEstado() == EstadoTropa.EM_VIAGEM_IDA) {
				if (restantes > 0) {
					t.setTurnosRestantes(restantes);
					tropaRepository.save(t);
				} else {
					chegouAoDestino(vila, t, turno);
				}
			} else if (restantes > 0) {
				t.setTurnosRestantes(restantes);
				tropaRepository.save(t);
			} else {
				retornou(vila, t, turno);
			}
		}
	}

	private void chegouAoDestino(Vila vila, Tropa t, int turno) {
		Optional<MasmorraAlvo> alvo = t.getMasmorraId() == null ? Optional.empty()
				: fonteMasmorras.buscarAtiva(vila.getId(), t.getMasmorraId());
		if (alvo.isPresent()) {
			batalhaService.resolver(t, alvo.get(), turno);
		} else {
			Map<String, Object> dados = new LinkedHashMap<>();
			dados.put("tropaId", t.getId());
			dados.put("tropaNome", t.getNome());
			dados.put("masmorraId", t.getMasmorraId());
			registro.registrar(vila, turno, TipoEventoTurno.MASMORRA,
					"A masmorra não existe mais: a tropa " + t.getNome() + " volta sem lutar.", dados);
		}
		t.setEstado(EstadoTropa.EM_VIAGEM_VOLTA);
		t.setTurnosRestantes(t.getTurnosViagem());
		tropaRepository.save(t);
	}

	private void retornou(Vila vila, Tropa t, int turno) {
		long membros = cidadaoRepository.countByTropaId(t.getId());
		aoRetornar(vila, t, turno);
		t.setEstado(EstadoTropa.AQUARTELADA);
		t.setMasmorraId(null);
		t.setRegiaoDestino(null);
		t.setTurnosViagem(null);
		t.setTurnosRestantes(null);
		tropaRepository.save(t);
		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("tropaId", t.getId());
		dados.put("tropaNome", t.getNome());
		dados.put("membros", membros);
		registro.registrar(vila, turno, TipoEventoTurno.TROPA_RETORNOU,
				"A tropa " + t.getNome() + " retornou ao quartel.", dados);
	}

	/**
	 * Gancho executado quando a tropa retorna ao quartel (sem efeito por padrão). As recompensas de
	 * masmorra são entregues na própria batalha, não na volta da tropa.
	 */
	protected void aoRetornar(Vila vila, Tropa tropa, int turno) {
		// Intencionalmente vazio.
	}
}
