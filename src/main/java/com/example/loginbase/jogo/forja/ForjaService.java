package com.example.loginbase.jogo.forja;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.Custo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrdemRepository;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.Estoque;
import com.example.loginbase.jogo.economia.VilaService;

/**
 * Cria ordens de forja para itens (armas e armaduras): valida nível/
 * quantidade e a capacidade da FORJA, garante fila de 1 por vila, debita o
 * custo total e agenda a conclusão. A entrega dos itens ({@code DISPONIVEL},
 * origem {@code FORJA}) acontece em {@code AplicadorOrdens}, disparada por
 * {@link VilaService#sincronizar} quando a ordem vence.
 */
@Service
@Transactional
public class ForjaService {

	private final VilaService vilaService;
	private final PredioRepository predioRepository;
	private final OrdemRepository ordemRepository;
	private final JogoProperties jogoProperties;
	private final Clock clock;

	public ForjaService(VilaService vilaService, PredioRepository predioRepository, OrdemRepository ordemRepository,
			JogoProperties jogoProperties, Clock clock) {
		this.vilaService = vilaService;
		this.predioRepository = predioRepository;
		this.ordemRepository = ordemRepository;
		this.jogoProperties = jogoProperties;
		this.clock = clock;
	}

	/**
	 * Cria uma ordem de forja de {@code quantidade} unidades de {@code modelo}
	 * no {@code nivel} informado.
	 *
	 * @throws RegraJogoException com {@link CodigoErro#REQUISICAO_INVALIDA} se {@code nivel} ou
	 *                            {@code quantidade} estiverem fora da faixa 1–5, com
	 *                            {@link CodigoErro#REQUISITO_NAO_ATENDIDO} se a FORJA estiver no
	 *                            nível 0 ou {@code nivel} exceder o nível da FORJA, com
	 *                            {@link CodigoErro#FILA_OCUPADA} se já houver uma ordem de forja
	 *                            pendente, ou com {@link CodigoErro#RECURSOS_INSUFICIENTES} se a
	 *                            vila não tiver o custo total da ordem
	 */
	public void forjar(long usuarioId, ModeloItem modelo, int nivel, int quantidade) {
		if (nivel < 1 || nivel > ModeloItem.NIVEL_MAXIMO || quantidade < 1
				|| quantidade > ModeloItem.QUANTIDADE_MAXIMA_ORDEM) {
			throw new RegraJogoException(CodigoErro.REQUISICAO_INVALIDA,
					"Nível (1-" + ModeloItem.NIVEL_MAXIMO + ") ou quantidade (1-" + ModeloItem.QUANTIDADE_MAXIMA_ORDEM
							+ ") de forja inválidos: nível=" + nivel + ", quantidade=" + quantidade);
		}

		Vila vila = vilaService.obterParaAtualizacao(usuarioId);

		int nivelForja = predioRepository.findByVilaId(vila.getId()).stream()
				.filter(predio -> predio.getTipo() == TipoPredio.FORJA)
				.findFirst()
				.map(Predio::getNivel)
				.orElse(0);
		if (nivelForja < 1) {
			throw new RegraJogoException(CodigoErro.REQUISITO_NAO_ATENDIDO, "Forja não construída");
		}
		if (nivel > nivelForja) {
			throw new RegraJogoException(CodigoErro.REQUISITO_NAO_ATENDIDO,
					"Nível " + nivel + " maior que o nível da forja (" + nivelForja + ")");
		}

		boolean filaOcupada = ordemRepository.findByVilaId(vila.getId()).stream()
				.anyMatch(ordem -> ordem.getCategoria() == CategoriaOrdem.FORJA);
		if (filaOcupada) {
			throw new RegraJogoException(CodigoErro.FILA_OCUPADA, "Já existe uma ordem de forja em andamento");
		}

		Custo custoTotal = modelo.custoTotal(nivel, quantidade);
		debitar(vila, custoTotal);

		long tempoTotalSegundos = modelo.tempoTotalSegundos(nivel, quantidade, jogoProperties.getVelocidade());
		Instant agora = clock.instant();
		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.FORJA);
		ordem.setAlvo(modelo.name());
		ordem.setNivel(nivel);
		ordem.setQuantidade(quantidade);
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(tempoTotalSegundos));
		ordemRepository.save(ordem);
	}

	/**
	 * Subtrai o custo total (em unidades inteiras) dos recursos da vila
	 * (guardados em milésimos): lança {@link CodigoErro#RECURSOS_INSUFICIENTES}
	 * via {@link Estoque#subtrair} caso a vila não tenha saldo de algum
	 * recurso.
	 */
	private void debitar(Vila vila, Custo custoTotal) {
		Estoque estoque = new Estoque(Map.of(
				TipoRecurso.COMIDA, vila.getComida(),
				TipoRecurso.MADEIRA, vila.getMadeira(),
				TipoRecurso.PEDRA, vila.getPedra(),
				TipoRecurso.FERRO, vila.getFerro()));

		for (TipoRecurso recurso : TipoRecurso.values()) {
			long quantidadeMilesimos = custoTotal.quantidade(recurso) * 1000L;
			if (quantidadeMilesimos > 0) {
				estoque = estoque.subtrair(recurso, quantidadeMilesimos);
			}
		}

		vila.setComida(estoque.get(TipoRecurso.COMIDA));
		vila.setMadeira(estoque.get(TipoRecurso.MADEIRA));
		vila.setPedra(estoque.get(TipoRecurso.PEDRA));
		vila.setFerro(estoque.get(TipoRecurso.FERRO));
	}

}
