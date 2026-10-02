package com.example.loginbase.jogo.masmorra;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/** Surgimento, evolução, ataque e remoção de masmorras (masmorras.md R1–R5). */
@Service
@Transactional
public class MasmorraService {

	static final double CHANCE_SURGIMENTO = 0.01;
	static final int MAX_ATIVAS = 3;
	static final int CARENCIA_TURNOS = 12;
	static final int TURNOS_PARA_EVOLUIR = 18;
	static final int TURNOS_BLOQUEIO_POS_LIMPEZA = 6;

	private final MasmorraRepository masmorraRepository;
	private final RegiaoRepository regiaoRepository;
	private final RegistroEventoTurnoService registroEventos;

	public MasmorraService(MasmorraRepository masmorraRepository, RegiaoRepository regiaoRepository,
			RegistroEventoTurnoService registroEventos) {
		this.masmorraRepository = masmorraRepository;
		this.regiaoRepository = regiaoRepository;
		this.registroEventos = registroEventos;
	}

	public void processar(Vila vila, int turno) {
		processar(vila, turno, new Random(vila.getSemente() * 31 + turno), CHANCE_SURGIMENTO);
	}

	void processar(Vila vila, int turno, Random rng, double chance) {
		evoluir(vila, turno);
		surgir(vila, turno, rng, chance);
	}

	private void evoluir(Vila vila, int turno) {
		for (Masmorra m : masmorraRepository.findByVilaIdAndAtivaTrueOrderByIdAsc(vila.getId())) {
			if (m.getTurnoSurgimento() >= turno
					|| (m.getTurnoUltimoAtaque() != null && m.getTurnoUltimoAtaque() == turno)) {
				continue;
			}
			m.setTurnosSemAtaque(m.getTurnosSemAtaque() + 1);
			if (m.getTurnosSemAtaque() >= TURNOS_PARA_EVOLUIR) {
				m.setTurnosSemAtaque(0);
				if (m.getNivel() < Masmorra.NIVEL_MAX) {
					m.setNivel(m.getNivel() + 1);
					registroEventos.registrar(vila, turno, TipoEventoTurno.MASMORRA,
							"A masmorra da região " + m.getRegiaoIndice() + " evoluiu para N" + m.getNivel(),
							Map.of("regiao", m.getRegiaoIndice(), "nivel", m.getNivel(), "evento", "EVOLUIU"));
				}
			}
			masmorraRepository.save(m);
		}
	}

	private void surgir(Vila vila, int turno, Random rng, double chance) {
		if (turno - vila.getTurnoCriacao() < CARENCIA_TURNOS) {
			return;
		}
		List<Masmorra> ativas = masmorraRepository.findByVilaIdAndAtivaTrueOrderByIdAsc(vila.getId());
		long total = ativas.size();
		if (total >= MAX_ATIVAS) {
			return;
		}
		Set<Integer> ocupadas = new HashSet<>();
		ativas.forEach(m -> ocupadas.add(m.getRegiaoIndice()));
		List<Regiao> regioes = regiaoRepository.findAllByVilaId(vila.getId()).stream()
				.sorted((a, b) -> Integer.compare(a.getIndice(), b.getIndice())).toList();
		for (Regiao r : regioes) {
			if (total >= MAX_ATIVAS) {
				break;
			}
			boolean elegivel = !r.isPossuida() && !ocupadas.contains(r.getIndice())
					&& (r.getLimpaAteTurno() == null || turno >= r.getLimpaAteTurno());
			if (!elegivel || rng.nextDouble() >= chance) {
				continue;
			}
			masmorraRepository.save(new Masmorra(vila.getId(), r.getIndice(), Masmorra.NIVEL_MIN, turno));
			total++;
			registroEventos.registrar(vila, turno, TipoEventoTurno.MASMORRA,
					"Uma masmorra surgiu na região " + r.getIndice(),
					Map.of("regiao", r.getIndice(), "nivel", Masmorra.NIVEL_MIN, "evento", "SURGIU"));
		}
	}

	public void registrarAtaque(Masmorra masmorra, int turno) {
		masmorra.setTurnosSemAtaque(0);
		masmorra.setTurnoUltimoAtaque(turno);
		masmorraRepository.save(masmorra);
	}

	/** Remove a masmorra; a região fica sem surgimento por 6 turnos (bloqueia até T+5). */
	public void remover(Masmorra masmorra, int turno) {
		masmorra.setAtiva(false);
		masmorraRepository.save(masmorra);
		regiaoRepository.findByVilaIdAndIndice(masmorra.getVilaId(), masmorra.getRegiaoIndice()).ifPresent(r -> {
			r.setLimpaAteTurno(turno + TURNOS_BLOQUEIO_POS_LIMPEZA);
			regiaoRepository.save(r);
		});
	}

}
