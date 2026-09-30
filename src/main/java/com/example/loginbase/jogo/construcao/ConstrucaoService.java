package com.example.loginbase.jogo.construcao;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.Custo;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.config.JogoProperties;
import com.example.loginbase.jogo.dominio.CategoriaOrdem;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrdemRepository;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.dominio.VilaRepository;
import com.example.loginbase.jogo.economia.Estoque;
import com.example.loginbase.jogo.economia.VilaService;

/**
 * Valida e executa melhorias de prédio da vila: obtém a vila já sincronizada
 * ({@link VilaService#obterParaAtualizacao}), aplica as cinco validações da
 * tabela A.3 (nível máximo, limite pelo centro da vila, pré-requisitos de
 * forja/quartel, fila de construção única e recursos suficientes), debita o
 * custo imediatamente e cria a {@link Ordem} de categoria
 * {@link CategoriaOrdem#CONSTRUCAO}. O efeito de conclusão (nível sobe, novo
 * canteiro se {@code FAZENDA}) é aplicado por {@code AplicadorOrdens} quando
 * {@code VilaService.sincronizar} conclui a ordem — não é responsabilidade
 * deste serviço.
 */
@Service
@Transactional
public class ConstrucaoService {

	private final VilaService vilaService;
	private final VilaRepository vilaRepository;
	private final PredioRepository predioRepository;
	private final OrdemRepository ordemRepository;
	private final JogoProperties jogoProperties;
	private final Clock clock;

	public ConstrucaoService(VilaService vilaService, VilaRepository vilaRepository,
			PredioRepository predioRepository, OrdemRepository ordemRepository, JogoProperties jogoProperties,
			Clock clock) {
		this.vilaService = vilaService;
		this.vilaRepository = vilaRepository;
		this.predioRepository = predioRepository;
		this.ordemRepository = ordemRepository;
		this.jogoProperties = jogoProperties;
		this.clock = clock;
	}

	/**
	 * Inicia a melhoria de {@code tipo} para o próximo nível na vila do
	 * usuário informado: valida (nesta ordem) nível máximo, limite pelo
	 * centro da vila, pré-requisitos de forja/quartel, fila de construção
	 * ocupada e recursos suficientes; debita o custo e cria a ordem de
	 * construção com o tempo de conclusão calculado.
	 *
	 * @throws RegraJogoException se qualquer validação falhar (o primeiro
	 *                            erro encontrado, na ordem acima, é o
	 *                            lançado)
	 */
	public void melhorar(long usuarioId, TipoPredio tipo) {
		Vila vila = vilaService.obterParaAtualizacao(usuarioId);

		Map<TipoPredio, Predio> prediosPorTipo = predioRepository.findByVilaId(vila.getId()).stream()
				.collect(Collectors.toMap(Predio::getTipo, predio -> predio));

		int nivelAtual = nivelDe(prediosPorTipo, tipo);
		if (nivelAtual >= TipoPredio.NIVEL_MAXIMO) {
			throw new RegraJogoException(CodigoErro.NIVEL_MAXIMO,
					"Prédio " + tipo + " já está no nível máximo (" + TipoPredio.NIVEL_MAXIMO + ")");
		}
		int nivelAlvo = nivelAtual + 1;

		if (tipo != TipoPredio.CENTRO_VILA) {
			int nivelCentro = nivelDe(prediosPorTipo, TipoPredio.CENTRO_VILA);
			if (nivelAlvo > nivelCentro) {
				throw new RegraJogoException(CodigoErro.REQUISITO_NAO_ATENDIDO,
						"Centro da vila está no nível " + nivelCentro + ": " + tipo
								+ " não pode passar desse nível");
			}
		}

		if (tipo == TipoPredio.FORJA && nivelDe(prediosPorTipo, TipoPredio.MINA_FERRO) < 1) {
			throw new RegraJogoException(CodigoErro.REQUISITO_NAO_ATENDIDO,
					"Forja exige mina de ferro no nível 1 ou superior");
		}
		if (tipo == TipoPredio.QUARTEL && nivelDe(prediosPorTipo, TipoPredio.FORJA) < 1) {
			throw new RegraJogoException(CodigoErro.REQUISITO_NAO_ATENDIDO,
					"Quartel exige forja no nível 1 ou superior");
		}

		boolean filaOcupada = ordemRepository.findByVilaId(vila.getId()).stream()
				.anyMatch(ordem -> ordem.getCategoria() == CategoriaOrdem.CONSTRUCAO);
		if (filaOcupada) {
			throw new RegraJogoException(CodigoErro.FILA_OCUPADA,
					"Já existe uma construção em andamento nesta vila");
		}

		Custo custo = tipo.custo(nivelAlvo, jogoProperties.curvaNiveis());
		debitar(vila, custo);

		Instant agora = clock.instant();
		long tempoEfetivoSegundos = tempoEfetivoSegundos(tipo, nivelAlvo);

		Ordem ordem = new Ordem();
		ordem.setVilaId(vila.getId());
		ordem.setCategoria(CategoriaOrdem.CONSTRUCAO);
		ordem.setAlvo(tipo.name());
		ordem.setNivel(nivelAlvo);
		ordem.setIniciadaEm(agora);
		ordem.setConcluiEm(agora.plusSeconds(tempoEfetivoSegundos));
		ordemRepository.save(ordem);

		vilaRepository.save(vila);
	}

	private int nivelDe(Map<TipoPredio, Predio> prediosPorTipo, TipoPredio tipo) {
		Predio predio = prediosPorTipo.get(tipo);
		return predio == null ? 0 : predio.getNivel();
	}

	/** Debita o custo (unidades inteiras) do estoque da vila (milésimos), recurso a recurso. */
	private void debitar(Vila vila, Custo custo) {
		Estoque estoque = new Estoque(Map.of(
				TipoRecurso.COMIDA, vila.getComida(),
				TipoRecurso.MADEIRA, vila.getMadeira(),
				TipoRecurso.PEDRA, vila.getPedra(),
				TipoRecurso.FERRO, vila.getFerro()));

		for (TipoRecurso recurso : TipoRecurso.values()) {
			long quantidade = custo.quantidade(recurso);
			if (quantidade > 0) {
				estoque = estoque.subtrair(recurso, quantidade * 1000L);
			}
		}

		vila.setComida(estoque.get(TipoRecurso.COMIDA));
		vila.setMadeira(estoque.get(TipoRecurso.MADEIRA));
		vila.setPedra(estoque.get(TipoRecurso.PEDRA));
		vila.setFerro(estoque.get(TipoRecurso.FERRO));
	}

	/** Tempo de conclusão em segundos, com a velocidade configurada aplicada: {@code ceil(tempoBase / velocidade)}. */
	private long tempoEfetivoSegundos(TipoPredio tipo, int nivelAlvo) {
		long tempoBaseSegundos = tipo.tempoSegundos(nivelAlvo);
		int velocidade = jogoProperties.getVelocidade();
		return (tempoBaseSegundos + velocidade - 1) / velocidade;
	}

}
