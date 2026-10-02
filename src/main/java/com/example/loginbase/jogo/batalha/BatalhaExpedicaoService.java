package com.example.loginbase.jogo.batalha;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.masmorra.EntregaRecompensasService;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.quartel.PosicaoTropa;
import com.example.loginbase.jogo.quartel.Tropa;
import com.example.loginbase.jogo.quartel.TropaRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Orquestra a batalha de uma tropa em expedição: inimigos da {@link FonteMasmorras} -> combatentes -> motor ->
 * consequências -> grava {@link Batalha} -> informa a porta -> evento BATALHA. NÃO altera o estado da tropa (viagem de
 * volta é responsabilidade de quem chama); apenas define {@code ultimaBatalhaId}.
 */
@Service
public class BatalhaExpedicaoService {

	private final FonteMasmorras fonteMasmorras;
	private final FabricaCombatente fabrica;
	private final MotorBatalha motor;
	private final ConsequenciasBatalhaService consequencias;
	private final BatalhaRepository batalhaRepository;
	private final CidadaoRepository cidadaoRepository;
	private final TropaRepository tropaRepository;
	private final VilaRepository vilaRepository;
	private final RegistroEventoTurnoService registro;
	private final EntregaRecompensasService entregaRecompensas;

	public BatalhaExpedicaoService(FonteMasmorras fonteMasmorras, FabricaCombatente fabrica, MotorBatalha motor,
			ConsequenciasBatalhaService consequencias, BatalhaRepository batalhaRepository,
			CidadaoRepository cidadaoRepository, TropaRepository tropaRepository, VilaRepository vilaRepository,
			RegistroEventoTurnoService registro, EntregaRecompensasService entregaRecompensas) {
		this.fonteMasmorras = fonteMasmorras;
		this.fabrica = fabrica;
		this.motor = motor;
		this.consequencias = consequencias;
		this.batalhaRepository = batalhaRepository;
		this.cidadaoRepository = cidadaoRepository;
		this.tropaRepository = tropaRepository;
		this.vilaRepository = vilaRepository;
		this.registro = registro;
		this.entregaRecompensas = entregaRecompensas;
	}

	/** Semente determinística de (vilaId, tropaId, turno). */
	public static long semente(long vilaId, long tropaId, int turno) {
		long h = vilaId * 0x9E3779B97F4A7C15L;
		h = (h ^ (h >>> 29)) + tropaId * 0xBF58476D1CE4E5B9L;
		h = (h ^ (h >>> 31)) + turno * 0x94D049BB133111EBL;
		return h ^ (h >>> 32);
	}

	@Transactional
	public Batalha resolver(Tropa tropa, MasmorraAlvo alvo, int turno) {
		Vila vila = vilaRepository.findById(tropa.getVilaId()).orElseThrow();
		long semente = semente(vila.getId(), tropa.getId(), turno);

		List<Combatente> inimigos = fonteMasmorras.gerarInimigos(alvo.nivel(), semente);
		List<Cidadao> membros = new ArrayList<>(cidadaoRepository.findByTropaId(tropa.getId()));
		membros.sort((a, b) -> Long.compare(a.getId(), b.getId()));
		List<Combatente> combatentes = new ArrayList<>();
		for (Cidadao c : membros) {
			if (!c.isVivo() || c.getEstado() != EstadoCidadao.SAUDAVEL) {
				continue; // ferido não luta
			}
			LinhaCombate linha = c.getPosicaoTropa() == PosicaoTropa.RETAGUARDA ? LinhaCombate.RETAGUARDA
					: LinhaCombate.FRENTE;
			combatentes.add(fabrica.criar(c, linha));
		}

		ResultadoBatalha resultado = motor.resolver(combatentes, inimigos, semente);
		var conseq = consequencias.aplicar(resultado, vila, turno, semente);

		boolean vitoria = resultado.resultado() == ResultadoCombate.VITORIA;
		Map<String, Object> recompensas = null;
		if (vitoria) {
			// Quem lutou menos os mortos; feridos recebem XP.
			List<Long> guerreirosXp = combatentes.stream().filter(c -> c.lado() == LadoCombate.TROPA)
					.map(Combatente::id).filter(id -> !conseq.mortos().contains(id)).toList();
			recompensas = entregaRecompensas.entregar(vila, turno, alvo.nivel(), semente, guerreirosXp);
		}

		Batalha b = new Batalha();
		b.setVilaId(vila.getId());
		b.setTropaId(tropa.getId());
		b.setTropaNome(tropa.getNome());
		b.setMasmorraId(alvo.id());
		b.setMasmorraNivel(alvo.nivel());
		b.setRegiaoIndice(alvo.regiaoIndice());
		b.setTurno(turno);
		b.setSemente(semente);
		b.setResultado(resultado.resultado());
		b.setRodadas(resultado.rodadas());
		b.setLog(new LogBatalha(resultado.pvFinal(), resultado.log()));
		b.setRecompensas(recompensas);
		b = batalhaRepository.saveAndFlush(b);

		tropa.setUltimaBatalhaId(b.getId());
		tropaRepository.save(tropa);

		fonteMasmorras.registrarResultado(alvo.id(), vitoria, turno);

		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("batalhaId", b.getId());
		dados.put("tropaId", tropa.getId());
		dados.put("tropaNome", tropa.getNome());
		dados.put("masmorraId", alvo.id());
		dados.put("resultado", resultado.resultado().name());
		dados.put("rodadas", resultado.rodadas());
		dados.put("feridos", conseq.feridos());
		dados.put("mortos", conseq.mortos());
		registro.registrar(vila, turno, TipoEventoTurno.BATALHA,
				"A tropa " + tropa.getNome() + " " + (vitoria ? "venceu" : "perdeu") + " a batalha em "
						+ resultado.rodadas() + " rodada(s).",
				dados);
		return b;
	}
}
