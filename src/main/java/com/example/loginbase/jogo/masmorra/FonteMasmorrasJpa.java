package com.example.loginbase.jogo.masmorra;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.batalha.Combatente;
import com.example.loginbase.jogo.batalha.FonteMasmorras;
import com.example.loginbase.jogo.batalha.MasmorraAlvo;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/** Fonte de masmorras da expedição baseada em JPA. */
@Component
@Transactional
public class FonteMasmorrasJpa implements FonteMasmorras {

	private final MasmorraRepository masmorraRepository;
	private final MasmorraService masmorraService;
	private final GeradorInimigos geradorInimigos;
	private final VilaRepository vilaRepository;
	private final RegistroEventoTurnoService registroEventos;

	public FonteMasmorrasJpa(MasmorraRepository masmorraRepository, MasmorraService masmorraService,
			GeradorInimigos geradorInimigos, VilaRepository vilaRepository,
			RegistroEventoTurnoService registroEventos) {
		this.masmorraRepository = masmorraRepository;
		this.masmorraService = masmorraService;
		this.geradorInimigos = geradorInimigos;
		this.vilaRepository = vilaRepository;
		this.registroEventos = registroEventos;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MasmorraAlvo> listarAtivas(long vilaId) {
		return masmorraRepository.findByVilaIdAndAtivaTrueOrderByIdAsc(vilaId).stream().map(this::alvo).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MasmorraAlvo> buscarAtiva(long vilaId, long masmorraId) {
		return masmorraRepository.findByIdAndVilaIdAndAtivaTrue(masmorraId, vilaId).map(this::alvo);
	}

	@Override
	public List<Combatente> gerarInimigos(int nivel, long semente) {
		return geradorInimigos.gerar(nivel, semente);
	}

	@Override
	public void registrarResultado(long masmorraId, boolean vitoria, int turno) {
		Masmorra m = masmorraRepository.findById(masmorraId).orElse(null);
		if (m == null || !m.isAtiva()) {
			return;
		}
		masmorraService.registrarAtaque(m, turno);
		if (vitoria) {
			masmorraService.remover(m, turno);
		}
		vilaRepository.findById(m.getVilaId()).ifPresent(vila -> {
			String texto = "A masmorra N" + m.getNivel() + " da região " + m.getRegiaoIndice()
					+ (vitoria ? " foi destruída" : " resistiu ao ataque");
			registroEventos.registrar(vila, turno, TipoEventoTurno.MASMORRA, texto, Map.of("regiao",
					m.getRegiaoIndice(), "nivel", m.getNivel(), "evento", vitoria ? "DESTRUIDA" : "RESISTIU"));
		});
	}

	private MasmorraAlvo alvo(Masmorra m) {
		return new MasmorraAlvo(m.getId(), m.getRegiaoIndice(), m.getNivel());
	}

}
