package com.example.loginbase.jogo.servico;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.dto.PreviaMapaDTO;
import com.example.loginbase.jogo.dto.RegiaoBonusDTO;
import com.example.loginbase.jogo.dto.RegiaoPreviaDTO;
import com.example.loginbase.jogo.excecao.PreviaExpiradaException;
import com.example.loginbase.jogo.excecao.PreviaNaoEncontradaException;
import com.example.loginbase.jogo.excecao.VilaJaExisteException;
import com.example.loginbase.jogo.modelo.VilaPrevia;
import com.example.loginbase.jogo.repositorio.VilaPreviaRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

/**
 * Prévia do mapa por usuário sem vila: uma única vigente, regenerada da semente a cada consulta.
 */
@Service
public class VilaPreviaService {

	private final VilaPreviaRepository previaRepository;
	private final VilaRepository vilaRepository;
	private final GeradorMapaService gerador;

	public VilaPreviaService(VilaPreviaRepository previaRepository, VilaRepository vilaRepository,
			GeradorMapaService gerador) {
		this.previaRepository = previaRepository;
		this.vilaRepository = vilaRepository;
		this.gerador = gerador;
	}

	/** Cria a primeira prévia (rodada 1) ou renova a existente (nova semente e id, rodada + 1). */
	@Transactional
	public PreviaMapaDTO gerar(Long usuarioId) {
		exigirSemVila(usuarioId);
		long semente = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
		VilaPrevia previa = previaRepository.findById(usuarioId).orElse(null);
		if (previa == null) {
			previa = new VilaPrevia(usuarioId, UUID.randomUUID(), semente, 1);
		} else {
			previa.setSemente(semente);
			previa.setPreviaId(UUID.randomUUID());
			previa.setRodada(previa.getRodada() + 1);
		}
		return montar(previaRepository.saveAndFlush(previa));
	}

	@Transactional(readOnly = true)
	public PreviaMapaDTO obter(Long usuarioId) {
		exigirSemVila(usuarioId);
		return montar(previaRepository.findById(usuarioId)
				.orElseThrow(() -> new PreviaNaoEncontradaException("Nenhuma prévia de mapa gerada")));
	}

	/** Exige que o previaId informado seja o da prévia vigente; devolve a prévia. */
	@Transactional(readOnly = true)
	public VilaPrevia exigirVigente(Long usuarioId, UUID previaId) {
		VilaPrevia previa = previaRepository.findById(usuarioId)
				.orElseThrow(() -> new PreviaExpiradaException("O mapa mudou. Escolha as regiões novamente"));
		if (previaId == null || !previa.getPreviaId().equals(previaId)) {
			throw new PreviaExpiradaException("O mapa mudou. Escolha as regiões novamente");
		}
		return previa;
	}

	@Transactional
	public void remover(Long usuarioId) {
		previaRepository.deleteById(usuarioId);
	}

	private void exigirSemVila(Long usuarioId) {
		if (vilaRepository.existsByUsuarioId(usuarioId)) {
			throw new VilaJaExisteException("Usuário já possui uma vila");
		}
	}

	private PreviaMapaDTO montar(VilaPrevia previa) {
		List<RegiaoPreviaDTO> regioes = gerador.gerar(previa.getSemente()).stream()
				.map(r -> new RegiaoPreviaDTO(r.indice(), r.tipo(), r.bonus().stream()
						.map(b -> new RegiaoBonusDTO(b.bonus(), b.posicao(), b.valor())).toList()))
				.toList();
		return new PreviaMapaDTO(previa.getPreviaId(), previa.getRodada(), regioes);
	}

}
