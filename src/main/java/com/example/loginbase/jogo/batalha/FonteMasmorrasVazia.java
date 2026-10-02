package com.example.loginbase.jogo.batalha;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** Implementação padrão enquanto não há masmorras: nenhuma ativa, nenhum inimigo. */
@Component
@ConditionalOnMissingBean(value = FonteMasmorras.class, ignored = FonteMasmorrasVazia.class)
public class FonteMasmorrasVazia implements FonteMasmorras {

	@Override
	public List<MasmorraAlvo> listarAtivas(long vilaId) {
		return List.of();
	}

	@Override
	public Optional<MasmorraAlvo> buscarAtiva(long vilaId, long masmorraId) {
		return Optional.empty();
	}

	@Override
	public List<Combatente> gerarInimigos(int nivel, long semente) {
		return List.of();
	}

	@Override
	public void registrarResultado(long masmorraId, boolean vitoria, int turno) {
		// sem masmorras
	}
}
