package com.example.loginbase.jogo.construcao;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Ponto único de checagem de ladrilho ocupado. Considera construções e ladrilhos marcados para coleta.
 */
@Component
public class VerificadorOcupacaoLadrilhos {

	private final ConstrucaoRepository construcaoRepository;

	private final ConstrucaoMarcacaoRepository marcacaoRepository;

	public VerificadorOcupacaoLadrilhos(ConstrucaoRepository construcaoRepository,
			ConstrucaoMarcacaoRepository marcacaoRepository) {
		this.construcaoRepository = construcaoRepository;
		this.marcacaoRepository = marcacaoRepository;
	}

	/** Chaves "x,y" dos ladrilhos ocupados da região. */
	public Set<String> ocupados(Long vilaId, int regiaoIndice) {
		Set<String> ocupados = new HashSet<>();
		for (Construcao c : construcaoRepository.findByVilaIdAndRegiaoIndice(vilaId, regiaoIndice)) {
			int tam = c.getTamanho() == null ? 1 : Math.max(c.getTamanho(), 1);
			for (int dx = 0; dx < tam; dx++) {
				for (int dy = 0; dy < tam; dy++) {
					ocupados.add(chave(c.getX() + dx, c.getY() + dy));
				}
			}
		}
		for (ConstrucaoMarcacao m : marcacaoRepository.findByVilaIdAndRegiaoIndice(vilaId, regiaoIndice)) {
			ocupados.add(chave(m.getX(), m.getY()));
		}
		return ocupados;
	}

	public boolean ocupado(Long vilaId, int regiaoIndice, int x, int y) {
		return ocupados(vilaId, regiaoIndice).contains(chave(x, y));
	}

	/** Verdadeiro se algum ladrilho do quadrado tamanho x tamanho a partir de (x, y) está ocupado. */
	public boolean areaOcupada(Long vilaId, int regiaoIndice, int x, int y, int tamanho) {
		Set<String> ocupados = ocupados(vilaId, regiaoIndice);
		for (int dx = 0; dx < tamanho; dx++) {
			for (int dy = 0; dy < tamanho; dy++) {
				if (ocupados.contains(chave(x + dx, y + dy))) {
					return true;
				}
			}
		}
		return false;
	}

	static String chave(int x, int y) {
		return x + "," + y;
	}

}
