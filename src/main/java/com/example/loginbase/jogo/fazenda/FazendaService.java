package com.example.loginbase.jogo.fazenda;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RegraJogoException;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.CanteiroRepository;
import com.example.loginbase.jogo.dominio.EstoqueSemente;
import com.example.loginbase.jogo.dominio.EstoqueSementeRepository;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.Vila;
import com.example.loginbase.jogo.economia.VilaService;

/**
 * Gerencia o plantio de cultivos nos canteiros da fazenda. O plantio é
 * instantâneo: escolhe um canteiro (posição de 1 até
 * {@link TipoPredio#numeroCanteiros(int)} do nível atual da FAZENDA), consome
 * 1 semente do cultivo (exceto {@code TRIGO}, que não exige semente) e
 * substitui o cultivo ativo, que passa a produzir
 * imediatamente. Como {@link VilaService#obterParaAtualizacao} já produziu
 * recursos até agora com o cultivo anterior antes desta ação alterar o
 * canteiro, a produção anterior ao plantio é preservada e a próxima
 * sincronização já conta a nova taxa.
 */
@Service
@Transactional
public class FazendaService {

	private final VilaService vilaService;
	private final PredioRepository predioRepository;
	private final CanteiroRepository canteiroRepository;
	private final EstoqueSementeRepository estoqueSementeRepository;
	private final Clock clock;

	public FazendaService(VilaService vilaService, PredioRepository predioRepository,
			CanteiroRepository canteiroRepository, EstoqueSementeRepository estoqueSementeRepository, Clock clock) {
		this.vilaService = vilaService;
		this.predioRepository = predioRepository;
		this.canteiroRepository = canteiroRepository;
		this.estoqueSementeRepository = estoqueSementeRepository;
		this.clock = clock;
	}

	/**
	 * Planta {@code cultivo} no canteiro {@code posicao} da vila do usuário.
	 *
	 * @throws RegraJogoException com {@link CodigoErro#CANTEIRO_INEXISTENTE} se a posição for
	 *                            menor que 1 ou maior que o número de canteiros do nível atual da FAZENDA
	 * @throws RegraJogoException com {@link CodigoErro#SEMENTE_INDISPONIVEL} se {@code cultivo}
	 *                            exigir semente e a vila não tiver nenhuma em estoque
	 */
	public void plantar(long usuarioId, int posicao, Cultivo cultivo) {
		Vila vila = vilaService.obterParaAtualizacao(usuarioId);

		int nivelFazenda = predioRepository.findByVilaId(vila.getId()).stream()
				.filter(predio -> predio.getTipo() == TipoPredio.FAZENDA)
				.findFirst()
				.map(Predio::getNivel)
				.orElse(0);
		int canteiros = nivelFazenda == 0 ? 0 : TipoPredio.FAZENDA.numeroCanteiros(nivelFazenda);
		if (posicao < 1 || posicao > canteiros) {
			throw new RegraJogoException(CodigoErro.CANTEIRO_INEXISTENTE,
					"Canteiro " + posicao + " inexistente: fazenda nível " + nivelFazenda + " tem " + canteiros
							+ " canteiros");
		}

		if (cultivo.exigeSemente()) {
			consumirSemente(vila.getId(), cultivo);
		}

		Instant agora = clock.instant();
		Canteiro canteiro = buscarOuCriarCanteiro(vila.getId(), posicao);
		canteiro.setCultivo(cultivo);
		canteiro.setPlantadoEm(agora);
		canteiroRepository.save(canteiro);
	}

	private void consumirSemente(Long vilaId, Cultivo cultivo) {
		List<EstoqueSemente> sementes = estoqueSementeRepository.findByVilaId(vilaId);
		Optional<EstoqueSemente> semente = sementes.stream()
				.filter(estoque -> estoque.getCultivo() == cultivo)
				.findFirst();
		if (semente.isEmpty() || semente.get().getQuantidade() <= 0) {
			throw new RegraJogoException(CodigoErro.SEMENTE_INDISPONIVEL,
					"Nenhuma semente de " + cultivo + " disponível na vila " + vilaId);
		}
		EstoqueSemente estoque = semente.get();
		estoque.setQuantidade(estoque.getQuantidade() - 1);
		estoqueSementeRepository.save(estoque);
	}

	private Canteiro buscarOuCriarCanteiro(Long vilaId, int posicao) {
		return canteiroRepository.findByVilaId(vilaId).stream()
				.filter(canteiro -> canteiro.getPosicao() == posicao)
				.findFirst()
				.orElseGet(() -> {
					Canteiro novo = new Canteiro();
					novo.setVilaId(vilaId);
					novo.setPosicao(posicao);
					return novo;
				});
	}

}
