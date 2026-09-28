package com.example.loginbase.jogo.quartel;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.CategoriaItem;
import com.example.loginbase.jogo.catalogo.ModeloItem;
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
 * Treina um lote de 1 a 15 unidades no quartel: valida o nível mínimo do
 * quartel para o tipo de tropa, a fila de treino (no máximo 1 ordem
 * {@code TREINO} por vila), a capacidade do exército
 * ({@code 3 × nível do quartel}, contando as unidades já existentes mais o
 * tamanho do lote), N armas do modelo exigido pelo tipo e nível informado
 * ({@code DISPONIVEL}, da mesma vila), N armaduras do modelo/nível
 * informados ({@code DISPONIVEL}, da mesma vila) e a comida necessária
 * ({@code comida_tipo × quantidade}).
 *
 * <p>Se todas as validações passarem, debita a comida da vila, reserva as N
 * menores armas e as N menores armaduras disponíveis (por id) e cria a
 * {@link Ordem} de categoria {@code TREINO} com {@code quantidade} igual ao
 * tamanho do lote. Os itens nunca voltam ao inventário: na conclusão da
 * ordem (aplicada por {@link com.example.loginbase.jogo.economia.AplicadorOrdens},
 * disparada por {@link VilaService#sincronizar}), as N unidades são criadas
 * {@code DISPONIVEL} (cada uma com seu próprio sorteio de nome/sobrenome) e
 * os itens passam a {@code EQUIPADO}, presos a elas.
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
	 * Ordena o treino de um lote de {@code quantidade} unidades do tipo
	 * {@code tipo}, reservando {@code quantidade} armas do modelo exigido pelo
	 * tipo (nível {@code armaNivel}) e {@code quantidade} armaduras do modelo
	 * {@code armaduraModelo} (nível {@code armaduraNivel}), todas até a
	 * conclusão da ordem. Retorna a ordem criada.
	 *
	 * @throws RegraJogoException com {@link CodigoErro#REQUISITO_NAO_ATENDIDO} se o nível do
	 *                            quartel for menor que o mínimo exigido pelo tipo
	 * @throws RegraJogoException com {@link CodigoErro#FILA_OCUPADA} se já houver uma ordem
	 *                            {@code TREINO} pendente na vila
	 * @throws RegraJogoException com {@link CodigoErro#CAPACIDADE_EXERCITO} se unidades
	 *                            existentes mais o tamanho do lote excederem {@code 3 × nível do
	 *                            quartel}
	 * @throws RegraJogoException com {@link CodigoErro#ITEM_INDISPONIVEL} se {@code armaduraModelo}
	 *                            não for uma armadura, ou se não houver {@code quantidade} armas ou
	 *                            armaduras {@code DISPONIVEL} da própria vila no modelo/nível pedidos
	 * @throws RegraJogoException com {@link CodigoErro#RECURSOS_INSUFICIENTES} se a vila não
	 *                            tiver comida suficiente para o lote
	 */
	public Ordem treinar(long usuarioId, TipoTropa tipo, int armaNivel, ModeloItem armaduraModelo, int armaduraNivel,
			int quantidade) {
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
		// acima); somamos sua quantidade mesmo assim por clareza com a regra de
		// negócio ("unidades existentes + Σ quantidade das ordens em andamento +
		// tamanho do lote pedido").
		int quantidadeEmOrdens = ordensDeTreino.stream().mapToInt(Ordem::getQuantidade).sum();
		if (unidadesAtuais + quantidadeEmOrdens + quantidade > capacidadeExercito) {
			throw new RegraJogoException(CodigoErro.CAPACIDADE_EXERCITO,
					"Capacidade do exército (" + capacidadeExercito + ") excedida na vila " + vila.getId());
		}

		if (armaduraModelo.categoria() != CategoriaItem.ARMADURA) {
			throw new RegraJogoException(CodigoErro.ITEM_INDISPONIVEL,
					armaduraModelo + " não é um modelo de armadura");
		}

		List<Item> armas = itemRepository.findByVilaIdAndModeloAndNivelAndStatusOrderByIdAsc(vila.getId(),
				tipo.armaExigida(), armaNivel, StatusItem.DISPONIVEL);
		if (armas.size() < quantidade) {
			throw new RegraJogoException(CodigoErro.ITEM_INDISPONIVEL,
					"Armas insuficientes para treinar " + quantidade + " " + tipo + ": exige " + tipo.armaExigida()
							+ " nível " + armaNivel + " DISPONIVEL da própria vila, há " + armas.size());
		}

		List<Item> armaduras = itemRepository.findByVilaIdAndModeloAndNivelAndStatusOrderByIdAsc(vila.getId(),
				armaduraModelo, armaduraNivel, StatusItem.DISPONIVEL);
		if (armaduras.size() < quantidade) {
			throw new RegraJogoException(CodigoErro.ITEM_INDISPONIVEL,
					"Armaduras insuficientes para treinar " + quantidade + " " + tipo + ": exige " + armaduraModelo
							+ " nível " + armaduraNivel + " DISPONIVEL da própria vila, há " + armaduras.size());
		}

		long comidaNecessaria = tipo.comida() * quantidade * MILESIMOS_POR_UNIDADE;
		if (vila.getComida() < comidaNecessaria) {
			throw new RegraJogoException(CodigoErro.RECURSOS_INSUFICIENTES,
					"Comida insuficiente para treinar " + quantidade + " " + tipo + ": necessário "
							+ (comidaNecessaria / MILESIMOS_POR_UNIDADE) + ", disponível "
							+ (vila.getComida() / MILESIMOS_POR_UNIDADE));
		}
		vila.setComida(vila.getComida() - comidaNecessaria);

		Instant agora = clock.instant();
		long tempoBaseSegundos = tipo.tempoTreinoSegundos();
		long tempoSegundos = (tempoBaseSegundos * quantidade + velocidade - 1) / velocidade;

		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.TREINO);
		ordem.setAlvo(tipo.name());
		ordem.setNivel(armaNivel);
		ordem.setQuantidade(quantidade);
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(tempoSegundos));
		ordem = ordemRepository.save(ordem);

		for (int i = 0; i < quantidade; i++) {
			Item arma = armas.get(i);
			arma.setStatus(StatusItem.RESERVADO);
			arma.setOrdemId(ordem.getId());
			itemRepository.save(arma);

			Item armadura = armaduras.get(i);
			armadura.setStatus(StatusItem.RESERVADO);
			armadura.setOrdemId(ordem.getId());
			itemRepository.save(armadura);
		}

		return ordem;
	}

}
