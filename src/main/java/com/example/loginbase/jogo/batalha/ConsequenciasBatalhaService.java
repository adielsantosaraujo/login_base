package com.example.loginbase.jogo.batalha;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.cidadao.MorteService;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Consequências para os membros abatidos: vitória 80% ferido / 20% morto; derrota 50% / 50%. Ferido fica FERIDO por
 * 6 turnos e PERMANECE na tropa; morto na derrota perde os itens equipados (destruídos) e, na vitória, os devolve ao
 * inventário via {@link MorteService}. O sorteio usa um {@link Random} derivado da semente da batalha, um por abatido,
 * na ordem em que caíram.
 */
@Service
public class ConsequenciasBatalhaService {

	public static final int TURNOS_FERIDO = 6;
	public static final double CHANCE_MORTE_VITORIA = 0.20;
	public static final double CHANCE_MORTE_DERROTA = 0.50;
	private static final long SALT = 0x9E3779B97F4A7C15L;

	private final CidadaoRepository cidadaoRepository;
	private final ItemRepository itemRepository;
	private final MorteService morteService;
	private final RegistroEventoTurnoService registro;

	public ConsequenciasBatalhaService(CidadaoRepository cidadaoRepository, ItemRepository itemRepository,
			MorteService morteService, RegistroEventoTurnoService registro) {
		this.cidadaoRepository = cidadaoRepository;
		this.itemRepository = itemRepository;
		this.morteService = morteService;
		this.registro = registro;
	}

	/** Cidadãos afetados, na ordem em que foram abatidos. */
	public record Consequencias(List<Long> feridos, List<Long> mortos) {
	}

	/** Sorteio puro: true = morto, false = ferido. Consome um {@code nextDouble}. */
	public static boolean sortearMorte(Random rng, boolean vitoria) {
		return rng.nextDouble() < (vitoria ? CHANCE_MORTE_VITORIA : CHANCE_MORTE_DERROTA);
	}

	@Transactional
	public Consequencias aplicar(ResultadoBatalha resultado, Vila vila, int turno, long semente) {
		boolean vitoria = resultado.resultado() == ResultadoCombate.VITORIA;
		Random rng = new Random(semente ^ SALT);
		List<Long> feridos = new ArrayList<>();
		List<Long> mortos = new ArrayList<>();
		for (Long id : resultado.abatidosTropa()) {
			Cidadao c = cidadaoRepository.findById(id).orElse(null);
			if (c == null || !c.isVivo()) {
				continue;
			}
			if (sortearMorte(rng, vitoria)) {
				if (!vitoria) {
					// Derrota: os itens do morto saem do jogo (antes da morte, que os devolveria ao inventário).
					itemRepository.deleteAll(itemRepository.findByCidadaoId(id));
					itemRepository.flush();
				}
				morteService.morrer(vila, c, "batalha", turno);
				mortos.add(id);
			} else {
				c.setEstado(EstadoCidadao.FERIDO);
				c.setFeridoAteTurno(turno + TURNOS_FERIDO);
				cidadaoRepository.save(c);
				Map<String, Object> dados = new LinkedHashMap<>();
				dados.put("cidadaoId", id);
				dados.put("nome", c.getNome());
				dados.put("feridoAteTurno", c.getFeridoAteTurno());
				registro.registrar(vila, turno, TipoEventoTurno.FERIDO, c.getNome() + " ficou ferido na batalha.",
						dados);
				feridos.add(id);
			}
		}
		return new Consequencias(feridos, mortos);
	}
}
