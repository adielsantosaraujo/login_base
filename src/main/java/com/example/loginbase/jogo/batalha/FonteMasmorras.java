package com.example.loginbase.jogo.batalha;

import java.util.List;
import java.util.Optional;

/**
 * Porta para as masmorras (implementadas só na Fase 5). A Fase 5 fornece um bean desta interface e a implementação
 * vazia {@link FonteMasmorrasVazia} deixa de ser registrada ({@code @ConditionalOnMissingBean}).
 */
public interface FonteMasmorras {

	/** Masmorras ativas da vila. */
	List<MasmorraAlvo> listarAtivas(long vilaId);

	/** Masmorra ativa com o id informado na vila, se ainda existir. */
	Optional<MasmorraAlvo> buscarAtiva(long vilaId, long masmorraId);

	/** Inimigos (lado INIMIGO, id = índice) da masmorra do nível informado, gerados de forma determinística. */
	List<Combatente> gerarInimigos(int nivel, long semente);

	/** Informa o desfecho da batalha à masmorra (vitória a encerra; derrota a mantém). */
	void registrarResultado(long masmorraId, boolean vitoria, int turno);
}
