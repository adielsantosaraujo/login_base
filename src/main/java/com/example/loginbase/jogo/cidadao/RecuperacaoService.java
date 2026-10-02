package com.example.loginbase.jogo.cidadao;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/** Recuperação de feridos (passo 11): FERIDO vira SAUDAVEL quando {@code turno >= ferido_ate_turno}. */
@Service
public class RecuperacaoService {

	private final CidadaoRepository cidadaoRepository;
	private final RegistroEventoTurnoService registro;

	public RecuperacaoService(CidadaoRepository cidadaoRepository, RegistroEventoTurnoService registro) {
		this.cidadaoRepository = cidadaoRepository;
		this.registro = registro;
	}

	@Transactional
	public void processar(Vila vila, int turno) {
		for (Cidadao c : cidadaoRepository.findByVilaIdAndVivoTrueAndEstado(vila.getId(), EstadoCidadao.FERIDO)) {
			if (c.getFeridoAteTurno() != null && turno < c.getFeridoAteTurno()) {
				continue;
			}
			c.setEstado(EstadoCidadao.SAUDAVEL);
			c.setFeridoAteTurno(null);
			cidadaoRepository.save(c);
			Map<String, Object> dados = new LinkedHashMap<>();
			dados.put("cidadaoId", c.getId());
			dados.put("nome", c.getNome());
			registro.registrar(vila, turno, TipoEventoTurno.RECUPERADO, c.getNome() + " se recuperou.", dados);
		}
	}
}
