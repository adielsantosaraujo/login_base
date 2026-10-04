package com.example.loginbase.jogo.construcao;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.GradeRegioes;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.servico.GeradorLadrilhoService;

/** Criação (início de obra) de construções N1 e consulta. */
@Service
public class ConstrucaoService {

	private final ConstrucaoRepository construcaoRepository;
	private final RegiaoRepository regiaoRepository;
	private final EstoqueService estoqueService;
	private final VerificadorOcupacaoLadrilhos verificadorOcupacao;

	public ConstrucaoService(ConstrucaoRepository construcaoRepository, RegiaoRepository regiaoRepository,
			EstoqueService estoqueService, VerificadorOcupacaoLadrilhos verificadorOcupacao) {
		this.construcaoRepository = construcaoRepository;
		this.regiaoRepository = regiaoRepository;
		this.estoqueService = estoqueService;
		this.verificadorOcupacao = verificadorOcupacao;
	}

	/**
	 * Valida região, posição e custo, debita o estoque e cria a construção N1 em EM_OBRA (atômico).
	 *
	 * @throws JogoException 400 violação de regra; 409 (RecursosInsuficientesException) se faltar recurso
	 */
	@Transactional
	public Construcao criar(Vila vila, TipoConstrucao tipo, int regiaoIndice, int x, int y) {
		if (tipo == null) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Tipo de construção obrigatório");
		}
		if (regiaoIndice < 1 || regiaoIndice > GradeRegioes.TOTAL) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Região inválida: " + regiaoIndice);
		}
		Regiao regiao = regiaoRepository.findByVilaIdAndIndice(vila.getId(), regiaoIndice)
				.filter(Regiao::isPossuida)
				.orElseThrow(() -> new JogoException(HttpStatus.BAD_REQUEST, "Região não possuída: " + regiaoIndice));
		if (!ConstrucaoCatalogo.permiteRegiao(tipo, regiao.getTipo())) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "%s só pode ser construído em região %s"
					.formatted(ConstrucaoCatalogo.de(tipo).nome(), ConstrucaoCatalogo.regioesPermitidas(tipo).stream().map(TipoRegiao::getNomeExibicao)
									.collect(java.util.stream.Collectors.joining(" ou "))));
		}
		NivelConstrucao nivel = NivelConstrucao.N1;
		int tamanho = ConstrucaoCatalogo.tamanho(nivel);
		int lado = GeradorLadrilhoService.LADO;
		if (x < 0 || y < 0 || x + tamanho > lado || y + tamanho > lado) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Posição fora da região");
		}
		if (verificadorOcupacao.areaOcupada(vila.getId(), regiaoIndice, x, y, tamanho)) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Ladrilho ocupado");
		}

		Map<Recurso, BigDecimal> custo = new EnumMap<>(Recurso.class);
		ConstrucaoCatalogo.custo(tipo, nivel).forEach((r, q) -> custo.put(r, BigDecimal.valueOf(q)));
		estoqueService.debitar(vila, custo);

		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(tipo);
		c.setNivel(nivel);
		c.setRegiaoIndice(regiaoIndice);
		c.setX(x);
		c.setY(y);
		c.setTamanho(tamanho);
		c.setEstado(EstadoConstrucao.EM_OBRA);
		c.setPoTotal(ConstrucaoCatalogo.po(tipo, nivel));
		c.setPoAtual(0);
		return construcaoRepository.save(c);
	}

	/**
	 * Inicia o upgrade de um prédio ATIVA para o próximo nível: valida espaço (ignorando o próprio prédio),
	 * debita o custo do próximo nível, passa a EM_UPGRADE e zera a PO com o total do novo nível.
	 */
	@Transactional
	public Construcao upgrade(Vila vila, Long id, NivelConstrucao novoNivel, Integer novaX, Integer novaY) {
		Construcao c = obter(vila, id);
		if (c.getEstado() != EstadoConstrucao.ATIVA) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Só é possível melhorar um prédio ativo");
		}
		NivelConstrucao atual = c.getNivel();
		if (atual == NivelConstrucao.N3) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Prédio já está no nível máximo");
		}
		NivelConstrucao proximo = NivelConstrucao.values()[atual.ordinal() + 1];
		if (novoNivel != null && novoNivel != proximo) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "O novo nível deve ser " + proximo);
		}
		int x = novaX == null ? c.getX() : novaX;
		int y = novaY == null ? c.getY() : novaY;
		int tamanho = ConstrucaoCatalogo.tamanho(proximo);
		int lado = GeradorLadrilhoService.LADO;
		if (x < 0 || y < 0 || x + tamanho > lado || y + tamanho > lado) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "Posição fora da região");
		}
		int tamAtual = Math.max(c.getTamanho() == null ? 1 : c.getTamanho(), 1);
		// a nova área deve conter a área atual do prédio
		if (x > c.getX() || y > c.getY() || x + tamanho < c.getX() + tamAtual || y + tamanho < c.getY() + tamAtual) {
			throw new JogoException(HttpStatus.BAD_REQUEST, "A nova área deve conter a área atual do prédio");
		}
		Set<String> ocupados = new HashSet<>(verificadorOcupacao.ocupados(vila.getId(), c.getRegiaoIndice()));
		for (int dx = 0; dx < tamAtual; dx++) {
			for (int dy = 0; dy < tamAtual; dy++) {
				ocupados.remove((c.getX() + dx) + "," + (c.getY() + dy));
			}
		}
		for (int dx = 0; dx < tamanho; dx++) {
			for (int dy = 0; dy < tamanho; dy++) {
				if (ocupados.contains((x + dx) + "," + (y + dy))) {
					throw new JogoException(HttpStatus.BAD_REQUEST, "Ladrilho ocupado");
				}
			}
		}

		Map<Recurso, BigDecimal> custo = new EnumMap<>(Recurso.class);
		ConstrucaoCatalogo.custo(c.getTipo(), proximo).forEach((r, q) -> custo.put(r, BigDecimal.valueOf(q)));
		estoqueService.debitar(vila, custo);

		c.setNivel(proximo);
		c.setX(x);
		c.setY(y);
		c.setTamanho(tamanho);
		c.setEstado(EstadoConstrucao.EM_UPGRADE);
		c.setPoTotal(ConstrucaoCatalogo.po(c.getTipo(), proximo));
		c.setPoAtual(0);
		return construcaoRepository.save(c);
	}

	@Transactional(readOnly = true)
	public Construcao obter(Vila vila, Long id) {
		Construcao c = construcaoRepository.findById(id)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Construção não encontrada"));
		if (!c.getVilaId().equals(vila.getId())) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Construção pertence a outra vila");
		}
		return c;
	}

	public List<CatalogoConstrucaoDTO> catalogo() {
		return ConstrucaoCatalogo.todos().stream().map(e -> {
			Map<String, Integer> custo = new java.util.LinkedHashMap<>();
			e.custoN1().forEach((r, q) -> custo.put(r.name(), q));
			return new CatalogoConstrucaoDTO(e.tipo(), e.nome(), ConstrucaoCatalogo.regioesPermitidas(e.tipo()),
					e.terreno(), custo,
					ConstrucaoCatalogo.tamanho(NivelConstrucao.N1), e.poN1(), e.profissoes());
		}).toList();
	}

}
