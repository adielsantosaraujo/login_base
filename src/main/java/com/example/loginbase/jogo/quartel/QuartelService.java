package com.example.loginbase.jogo.quartel;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.CategoriaItem;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrdemRepository;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.VilaService;

/**
 * Treina unidades no quartel: valida o nível mínimo do quartel para o tipo de
 * tropa, a arma (modelo exigido pelo tipo, {@code DISPONIVEL}, da mesma
 * vila), a armadura (qualquer modelo de categoria {@code ARMADURA},
 * {@code DISPONIVEL}, da mesma vila), a fila de treino (no máximo 1 ordem
 * {@code TREINO} por vila) e a capacidade do exército
 * ({@code 3 × nível do quartel}, contando as unidades já existentes mais a
 * ordem que esta chamada está prestes a criar).
 *
 * <p>Se todas as validações passarem, debita a comida do tipo da vila, marca
 * a arma e a armadura como {@code RESERVADO} e cria a {@link Ordem} de
 * categoria {@code TREINO}. Os itens nunca voltam ao inventário: na
 * conclusão da ordem (aplicada por
 * {@link com.example.loginbase.jogo.economia.AplicadorOrdens}, disparada por
 * {@link VilaService#sincronizar}), a unidade é criada {@code DISPONIVEL} e
 * os itens passam a {@code EQUIPADO}, presos a ela.
 */
@Service
@Transactional
public class QuartelService {

	/** 1 unidade de recurso = 1000 milésimos (ver {@code Vila}/{@code Estoque}). */
	private static final long MILESIMOS_POR_UNIDADE = 1000L;

	private final VilaService vilaService;
	private final PredioRepository predioRepository;
	private final ItemRepository itemRepository;
	private final UnidadeRepository unidadeRepository;
	private final OrdemRepository ordemRepository;
	private final Clock clock;
	private final int velocidade;

	public QuartelService(VilaService vilaService, PredioRepository predioRepository, ItemRepository itemRepository,
			UnidadeRepository unidadeRepository, OrdemRepository ordemRepository, JogoProperties jogoProperties,
			Clock clock) {
		this.vilaService = vilaService;
		this.predioRepository = predioRepository;
		this.itemRepository = itemRepository;
		this.unidadeRepository = unidadeRepository;
		this.ordemRepository = ordemRepository;
		this.clock = clock;
		this.velocidade = jogoProperties.getVelocidade();
	}

	/**
	 * Ordena o treino de uma unidade {@code tipo}, equipando a arma
	 * {@code armaId} e a armadura {@code armaduraId} (ambas reservadas até a
	 * conclusão da ordem).
	 *
	 * @throws RegraJogoException com {@link CodigoErro#REQUISITO_NAO_ATENDIDO} se o nível do
	 *                            quartel for menor que o mínimo exigido pelo tipo
	 * @throws RegraJogoException com {@link CodigoErro#ITEM_INDISPONIVEL} se a arma ou a
	 *                            armadura não existirem, não estiverem {@code DISPONIVEL}, não
	 *                            forem do modelo/categoria esperado, ou forem de outra vila
	 * @throws RegraJogoException com {@link CodigoErro#FILA_OCUPADA} se já houver uma ordem
	 *                            {@code TREINO} pendente na vila
	 * @throws RegraJogoException com {@link CodigoErro#CAPACIDADE_EXERCITO} se unidades
	 *                            existentes mais esta ordem excederem {@code 3 × nível do quartel}
	 * @throws RegraJogoException com {@link CodigoErro#RECURSOS_INSUFICIENTES} se a vila não
	 *                            tiver comida suficiente para o tipo
	 */
	public void treinar(long usuarioId, TipoTropa tipo, long armaId, long armaduraId) {
		Vila vila = vilaService.obterParaAtualizacao(usuarioId);

		int nivelQuartel = predioRepository.findByVilaId(vila.getId()).stream()
				.filter(predio -> predio.getTipo() == TipoPredio.QUARTEL)
				.findFirst()
				.map(Predio::getNivel)
				.orElse(0);
		if (nivelQuartel < tipo.nivelMinimoQuartel()) {
			throw new RegraJogoException(CodigoErro.REQUISITO_NAO_ATENDIDO,
					"Quartel nível " + nivelQuartel + " não atende ao mínimo (" + tipo.nivelMinimoQuartel()
							+ ") exigido para treinar " + tipo);
		}

		Item arma = itemRepository.findById(armaId)
				.filter(item -> item.getVilaId().equals(vila.getId()))
				.filter(item -> item.getStatus() == StatusItem.DISPONIVEL)
				.filter(item -> item.getModelo() == tipo.armaExigida())
				.orElseThrow(() -> new RegraJogoException(CodigoErro.ITEM_INDISPONIVEL,
						"Arma " + armaId + " indisponível para treinar " + tipo + " (exige " + tipo.armaExigida()
								+ " DISPONIVEL da própria vila)"));

		Item armadura = itemRepository.findById(armaduraId)
				.filter(item -> item.getVilaId().equals(vila.getId()))
				.filter(item -> item.getStatus() == StatusItem.DISPONIVEL)
				.filter(item -> item.getModelo().categoria() == CategoriaItem.ARMADURA)
				.orElseThrow(() -> new RegraJogoException(CodigoErro.ITEM_INDISPONIVEL,
						"Armadura " + armaduraId + " indisponível para treinar " + tipo
								+ " (exige armadura DISPONIVEL da própria vila)"));

		List<Ordem> ordensDeTreino = ordemRepository.findByVilaId(vila.getId()).stream()
				.filter(ordem -> ordem.getCategoria() == CategoriaOrdem.TREINO)
				.toList();
		if (!ordensDeTreino.isEmpty()) {
			throw new RegraJogoException(CodigoErro.FILA_OCUPADA,
					"Já existe uma ordem de treino em andamento na vila " + vila.getId());
		}

		int unidadesAtuais = unidadeRepository.findByVilaId(vila.getId()).size();
		int capacidadeExercito = TipoPredio.QUARTEL.capacidadeExercito(nivelQuartel);
		// ordensDeTreino já está vazia aqui (senão FILA_OCUPADA teria sido lançado
		// acima); somamos seu tamanho mesmo assim por clareza com a regra de
		// negócio ("unidades existentes + ordens em andamento"), e o "+1" conta a
		// ordem que esta chamada está prestes a criar.
		if (unidadesAtuais + ordensDeTreino.size() + 1 > capacidadeExercito) {
			throw new RegraJogoException(CodigoErro.CAPACIDADE_EXERCITO,
					"Capacidade do exército (" + capacidadeExercito + ") excedida na vila " + vila.getId());
		}

		long comidaNecessaria = tipo.comida() * MILESIMOS_POR_UNIDADE;
		if (vila.getComida() < comidaNecessaria) {
			throw new RegraJogoException(CodigoErro.RECURSOS_INSUFICIENTES,
					"Comida insuficiente para treinar " + tipo + ": necessário " + tipo.comida()
							+ ", disponível " + (vila.getComida() / MILESIMOS_POR_UNIDADE));
		}
		vila.setComida(vila.getComida() - comidaNecessaria);

		arma.setStatus(StatusItem.RESERVADO);
		itemRepository.save(arma);
		armadura.setStatus(StatusItem.RESERVADO);
		itemRepository.save(armadura);

		Instant agora = clock.instant();
		long tempoBaseSegundos = tipo.tempoTreinoSegundos();
		long tempoSegundos = (tempoBaseSegundos + velocidade - 1) / velocidade;

		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.TREINO);
		ordem.setAlvo(tipo.name());
		ordem.setQuantidade(1);
		ordem.setArmaItemId(arma.getId());
		ordem.setArmaduraItemId(armadura.getId());
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(tempoSegundos));
		ordemRepository.save(ordem);
	}

}
