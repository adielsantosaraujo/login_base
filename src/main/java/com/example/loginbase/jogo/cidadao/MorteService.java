package com.example.loginbase.jogo.cidadao;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Serviço compartilhado de morte (idade, fome, batalha): marca morto, desaloca, viúva o cônjuge,
 * registra o evento MORTE e faz a sucessão do líder. Itens ainda não existem (nada a devolver).
 */
@Service
public class MorteService {

	public static final int IDADE_ADULTO_ANOS = 18;

	private final CidadaoRepository cidadaoRepository;
	private final VilaRepository vilaRepository;
	private final JogoTurnoRepository turnoRepository;
	private final RegistroEventoTurnoService registro;

	public MorteService(CidadaoRepository cidadaoRepository, VilaRepository vilaRepository,
			JogoTurnoRepository turnoRepository, RegistroEventoTurnoService registro) {
		this.cidadaoRepository = cidadaoRepository;
		this.vilaRepository = vilaRepository;
		this.turnoRepository = turnoRepository;
		this.registro = registro;
	}

	/** Usa a vila do cidadão e o turno corrente (maior número de turno). */
	@Transactional
	public void morrer(Cidadao cidadao, String causa) {
		Vila vila = vilaRepository.findById(cidadao.getVilaId()).orElseThrow();
		morrer(vila, cidadao, causa, turnoRepository.maiorNumero());
	}

	@Transactional
	public void morrer(Vila vila, Cidadao cidadao, String causa, int turno) {
		if (!cidadao.isVivo()) {
			return;
		}
		cidadao.setVivo(false);
		cidadao.setConstrucaoId(null);
		Long conjugeId = cidadao.getConjugeId();
		cidadao.setConjugeId(null);
		cidadaoRepository.save(cidadao);
		if (conjugeId != null) {
			cidadaoRepository.findById(conjugeId).ifPresent(c -> {
				c.setConjugeId(null);
				cidadaoRepository.save(c);
			});
		}
		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("causa", causa);
		dados.put("cidadaoId", cidadao.getId());
		dados.put("nome", cidadao.getNome());
		dados.put("idadeAnos", cidadao.getIdadeAnos());
		registro.registrar(vila, turno, TipoEventoTurno.MORTE,
				cidadao.getNome() + " morreu (" + causa + ").", dados);
		sucederLider(vila, cidadao, turno);
	}

	private void sucederLider(Vila vila, Cidadao morto, int turno) {
		if (vila.getFamiliaLiderId() == null || !vila.getFamiliaLiderId().equals(morto.getFamiliaId())) {
			return;
		}
		boolean temAdulto = cidadaoRepository.findByFamiliaIdAndVivoTrue(morto.getFamiliaId()).stream()
				.anyMatch(c -> c.getIdadeAnos() >= IDADE_ADULTO_ANOS);
		if (temAdulto) {
			return;
		}
		Long familiaId = vila.getFamiliaLiderId();
		vila.setFamiliaLiderId(null);
		vilaRepository.save(vila);
		registro.registrar(vila, turno, TipoEventoTurno.SUCESSAO_LIDER,
				"A família líder ficou sem adultos vivos; a vila está sem líder.",
				Map.of("familiaId", familiaId, "motivo", "sem_adulto_vivo"));
	}

}
