package com.example.loginbase.jogo.construcao;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.LadrilhoRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.servico.GeradorLadrilhoService;

/** Marcação de ladrilhos de coleta de um prédio de coleta. */
@Service
public class MarcacaoService {

	private static final int[][] VIZINHOS = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

	private final ConstrucaoRepository construcaoRepository;
	private final ConstrucaoMarcacaoRepository marcacaoRepository;
	private final RegiaoRepository regiaoRepository;
	private final LadrilhoRepository ladrilhoRepository;

	public MarcacaoService(ConstrucaoRepository construcaoRepository, ConstrucaoMarcacaoRepository marcacaoRepository,
			RegiaoRepository regiaoRepository, LadrilhoRepository ladrilhoRepository) {
		this.construcaoRepository = construcaoRepository;
		this.marcacaoRepository = marcacaoRepository;
		this.regiaoRepository = regiaoRepository;
		this.ladrilhoRepository = ladrilhoRepository;
	}

	/** Máximo de ladrilhos marcados por nível: N1 4, N2 10, N3 20. */
	public static int limite(NivelConstrucao nivel) {
		return switch (nivel) {
			case N1 -> 4;
			case N2 -> 10;
			case N3 -> 20;
		};
	}

	@Transactional
	public ConstrucaoMarcacao marcar(Vila vila, Long construcaoId, Integer x, Integer y) {
		Construcao c = obterDaVila(vila, construcaoId);
		if (x == null || y == null) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Coordenadas x e y obrigatórias");
		}
		if (!ConstrucaoCatalogo.ehPredioDeColeta(c.getTipo())) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Construção não é um prédio de coleta");
		}
		int lado = GeradorLadrilhoService.LADO;
		if (x < 0 || y < 0 || x >= lado || y >= lado) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Ladrilho fora da região");
		}
		Set<String> doPredio = ladrilhosDoPredio(c);
		if (doPredio.contains(chave(x, y))) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Ladrilho ocupado pelo próprio prédio");
		}
		for (Construcao outra : construcaoRepository.findByVilaIdAndRegiaoIndice(vila.getId(), c.getRegiaoIndice())) {
			if (cobre(outra, x, y)) {
				throw new JogoException(HttpStatus.BAD_REQUEST, "Ladrilho ocupado por uma construção");
			}
		}
		if (marcacaoRepository.existsByVilaIdAndRegiaoIndiceAndXAndY(vila.getId(), c.getRegiaoIndice(), x, y)) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Ladrilho já marcado");
		}
		TipoTerreno esperado = ConstrucaoCatalogo.terreno(c.getTipo()).orElse(null);
		Regiao regiao = regiaoRepository.findByVilaIdAndIndice(vila.getId(), c.getRegiaoIndice())
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Região não encontrada"));
		TipoTerreno atual = ladrilhoRepository.findByRegiaoIdAndXAndY(regiao.getId(), x, y).map(Ladrilho::getTerreno)
				.orElse(null);
		if (esperado == null || atual != esperado) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Ladrilho sem o terreno do prédio (%s)"
					.formatted(esperado == null ? "-" : esperado.getNomeExibicao()));
		}
		List<ConstrucaoMarcacao> marcadas = marcacaoRepository.findByConstrucaoId(c.getId());
		if (marcadas.size() >= limite(c.getNivel())) {
			throw new JogoException(HttpStatus.BAD_REQUEST,
					"Limite de %d ladrilhos marcados atingido".formatted(limite(c.getNivel())));
		}
		Set<String> conectados = new HashSet<>(doPredio);
		marcadas.forEach(m -> conectados.add(chave(m.getX(), m.getY())));
		if (!temVizinho(conectados, x, y)) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Ladrilho sem conexão ortogonal com o prédio");
		}
		return marcacaoRepository.save(new ConstrucaoMarcacao(vila.getId(), c.getRegiaoIndice(), x, y, c.getId()));
	}

	@Transactional
	public void desmarcar(Vila vila, Long construcaoId, int x, int y) {
		Construcao c = obterDaVila(vila, construcaoId);
		List<ConstrucaoMarcacao> marcadas = marcacaoRepository.findByConstrucaoId(c.getId());
		ConstrucaoMarcacao alvo = marcadas.stream().filter(m -> m.getX() == x && m.getY() == y).findFirst()
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Marcação não encontrada"));
		List<ConstrucaoMarcacao> restantes = new ArrayList<>(marcadas);
		restantes.remove(alvo);
		Set<String> alcancados = new HashSet<>(ladrilhosDoPredio(c));
		ArrayDeque<String> fila = new ArrayDeque<>(alcancados);
		Set<String> pendentes = new HashSet<>();
		restantes.forEach(m -> pendentes.add(chave(m.getX(), m.getY())));
		while (!fila.isEmpty()) {
			String[] p = fila.poll().split(",");
			int px = Integer.parseInt(p[0]);
			int py = Integer.parseInt(p[1]);
			for (int[] d : VIZINHOS) {
				String k = chave(px + d[0], py + d[1]);
				if (pendentes.remove(k)) {
					alcancados.add(k);
					fila.add(k);
				}
			}
		}
		if (!pendentes.isEmpty()) {
			throw new JogoException(HttpStatus.BAD_REQUEST,
					"Desmarcar este ladrilho desconectaria outros ladrilhos marcados");
		}
		marcacaoRepository.delete(alvo);
	}

	@Transactional(readOnly = true)
	public List<ConstrucaoMarcacao> listar(Vila vila, Long construcaoId) {
		Construcao c = obterDaVila(vila, construcaoId);
		return marcacaoRepository.findByConstrucaoId(c.getId());
	}

	private Construcao obterDaVila(Vila vila, Long id) {
		Construcao c = construcaoRepository.findById(id)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Construção não encontrada"));
		if (!c.getVilaId().equals(vila.getId())) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Construção pertence a outra vila");
		}
		return c;
	}

	private static boolean cobre(Construcao c, int x, int y) {
		int tam = c.getTamanho() == null ? 1 : Math.max(c.getTamanho(), 1);
		return x >= c.getX() && x < c.getX() + tam && y >= c.getY() && y < c.getY() + tam;
	}

	private static Set<String> ladrilhosDoPredio(Construcao c) {
		Set<String> s = new HashSet<>();
		int tam = c.getTamanho() == null ? 1 : Math.max(c.getTamanho(), 1);
		for (int dx = 0; dx < tam; dx++) {
			for (int dy = 0; dy < tam; dy++) {
				s.add(chave(c.getX() + dx, c.getY() + dy));
			}
		}
		return s;
	}

	private static boolean temVizinho(Set<String> conjunto, int x, int y) {
		for (int[] d : VIZINHOS) {
			if (conjunto.contains(chave(x + d[0], y + d[1]))) {
				return true;
			}
		}
		return false;
	}

	private static String chave(int x, int y) {
		return x + "," + y;
	}

}
