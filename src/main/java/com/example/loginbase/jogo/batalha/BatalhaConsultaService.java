package com.example.loginbase.jogo.batalha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.Vila;

import lombok.RequiredArgsConstructor;

/** Consulta de batalhas gravadas (somente leitura). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatalhaConsultaService {

	private final BatalhaRepository repository;

	public List<ResumoBatalhaDTO> listar(Vila vila) {
		return repository.findByVilaIdOrderByTurnoDescIdDesc(vila.getId()).stream().map(ResumoBatalhaDTO::de).toList();
	}

	public DetalheBatalhaDTO detalhe(Vila vila, Long id) {
		Batalha b = repository.findByIdAndVilaId(id, vila.getId())
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Batalha não encontrada"));
		LogBatalha log = b.getLog();
		List<ResultadoBatalha.PvFinal> parts = log == null || log.participantes() == null ? List.of()
				: log.participantes();
		List<AcaoBatalha> acoes = log == null || log.acoes() == null ? List.of() : log.acoes();

		Map<String, String> nomes = new HashMap<>();
		for (ResultadoBatalha.PvFinal p : parts) {
			nomes.put(p.lado() + ":" + p.id(), p.nome());
		}
		Map<Integer, List<DetalheBatalhaDTO.Acao>> porRodada = new TreeMap<>();
		for (AcaoBatalha a : acoes) {
			DetalheBatalhaDTO.Ref atacante = new DetalheBatalhaDTO.Ref(a.atacanteId(), a.atacanteLado(),
					nomes.getOrDefault(a.atacanteLado() + ":" + a.atacanteId(), "?"));
			DetalheBatalhaDTO.Ref alvo = new DetalheBatalhaDTO.Ref(a.alvoId(), a.alvoLado(),
					nomes.getOrDefault(a.alvoLado() + ":" + a.alvoId(), "?"));
			porRodada.computeIfAbsent(a.rodada(), k -> new ArrayList<>()).add(new DetalheBatalhaDTO.Acao(atacante,
					alvo, a.dano(), a.critico(), a.pvAntes(), a.pvDepois(), a.abatido()));
		}
		List<DetalheBatalhaDTO.Rodada> rodadas = porRodada.entrySet().stream()
				.map(e -> new DetalheBatalhaDTO.Rodada(e.getKey(), e.getValue())).toList();
		List<DetalheBatalhaDTO.Participante> participantes = parts.stream()
				.map(p -> new DetalheBatalhaDTO.Participante(p.id(), p.lado(), p.nome(), p.pvMax(), p.pv())).toList();
		return new DetalheBatalhaDTO(b.getId(), b.getTurno(), b.getTropaId(), b.getTropaNome(), b.getMasmorraId(),
				b.getMasmorraNivel(), b.getRegiaoIndice(), b.getResultado(), b.getRodadas(), b.getCriadoEm(),
				participantes, rodadas, b.getRecompensas());
	}
}
