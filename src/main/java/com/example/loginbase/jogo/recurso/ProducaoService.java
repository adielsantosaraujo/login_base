package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.construcao.CatalogoFabricas;
import com.example.loginbase.jogo.construcao.CatalogoFabricas.Receita;
import com.example.loginbase.jogo.construcao.CatalogoPrediosProducao;
import com.example.loginbase.jogo.construcao.CatalogoPrediosProducao.Saida;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores.Trabalhador;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoCatalogo;
import com.example.loginbase.jogo.construcao.ConstrucaoMarcacao;
import com.example.loginbase.jogo.construcao.ConstrucaoMarcacaoRepository;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.LadrilhoRepository;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.servico.BonusTerrenoService;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Passo 1 do turno: produção. Estrutura:
 * <ul>
 * <li>{@link #processarProducao(Vila, int)}: ponto de entrada da etapa; chama, em ordem,
 * {@link #processarProducaoColataRural} e {@link #processarProducaoFabricas}.</li>
 * <li>{@link #processarProducaoColataRural}: prédios ATIVA de coleta e fazendas (implementado).</li>
 * <li>{@link #processarProducaoFabricas}: PONTO DE EXTENSÃO, vazio; a tarefa das fábricas o preenche
 * (consome insumos e credita via {@link EstoqueService}; use {@link #registrarProducao} para o evento
 * com {@code categoria="FABRICA"}).</li>
 * </ul>
 * Coleta: trabalhadores produtivos = min(alocados, floor(marcados válidos / 2)); contam os de maior
 * eficiência. Rural: todos os alocados. Produção = soma das eficiências (que já incluem o multiplicador do
 * nível) x base. O crédito não aplica limite (isso é o passo 4 do turno).
 */
@Service
public class ProducaoService {

	public static final String CATEGORIA_COLETA = "COLETA";
	public static final String CATEGORIA_RURAL = "RURAL";
	public static final String CATEGORIA_FABRICA = "FABRICA";

	private final ConstrucaoRepository construcaoRepository;
	private final ConstrucaoMarcacaoRepository marcacaoRepository;
	private final ConsultaTrabalhadores consultaTrabalhadores;
	private final EstoqueService estoqueService;
	private final RegistroEventoTurnoService registro;
	private final RegiaoRepository regiaoRepository;
	private final LadrilhoRepository ladrilhoRepository;
	private final BonusTerrenoService bonusTerrenoService;

	public ProducaoService(ConstrucaoRepository construcaoRepository,
			ConstrucaoMarcacaoRepository marcacaoRepository, ConsultaTrabalhadores consultaTrabalhadores,
			EstoqueService estoqueService, RegistroEventoTurnoService registro,
			RegiaoRepository regiaoRepository, LadrilhoRepository ladrilhoRepository,
			BonusTerrenoService bonusTerrenoService) {
		this.construcaoRepository = construcaoRepository;
		this.marcacaoRepository = marcacaoRepository;
		this.consultaTrabalhadores = consultaTrabalhadores;
		this.estoqueService = estoqueService;
		this.registro = registro;
		this.regiaoRepository = regiaoRepository;
		this.ladrilhoRepository = ladrilhoRepository;
		this.bonusTerrenoService = bonusTerrenoService;
	}

	@Transactional
	public void processarProducao(Vila vila, int turno) {
		processarProducaoColataRural(vila, turno);
		processarProducaoFabricas(vila, turno);
	}

	@Transactional
	public void processarProducaoColataRural(Vila vila, int turno) {
		List<Construcao> predios = construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.ATIVA)
				.stream().filter(c -> CatalogoPrediosProducao.produz(c.getTipo()))
				.sorted(Comparator.comparing(Construcao::getId)).toList();
		for (Construcao c : predios) {
			int bonus = bonusTerrenoService.bonusDoPredio(vila.getId(), c);
			BigDecimal fator = fator(bonus);
			boolean coleta = CatalogoPrediosProducao.ehColeta(c.getTipo());
			List<Trabalhador> trabalhadores = consultaTrabalhadores.trabalhadores(c, vila);
			List<Trabalhador> produtivos = coleta
					? selecionarProdutivos(trabalhadores, marcadosValidos(vila, c))
					: trabalhadores;
			double somaEficiencia = produtivos.stream().mapToDouble(Trabalhador::eficiencia).sum();
			if (somaEficiencia <= 0) {
				continue;
			}
			Map<Recurso, BigDecimal> produzido = new LinkedHashMap<>();
			for (Saida s : CatalogoPrediosProducao.saidas(c.getTipo(), c.getConfiguracao())) {
				BigDecimal q = BigDecimal.valueOf(somaEficiencia).multiply(BigDecimal.valueOf(s.base()))
						.multiply(fator).setScale(2, RoundingMode.HALF_UP);
				if (q.signum() > 0) {
					produzido.merge(s.recurso(), q, BigDecimal::add);
				}
			}
			registrarProducao(vila, turno, c, coleta ? CATEGORIA_COLETA : CATEGORIA_RURAL, produtivos.size(),
					produzido, bonus > 0 ? Map.of("bonusTerreno", bonus) : Map.of());
		}
	}

	/**
	 * Produção das fábricas (ATIVA), em ordem fixa de tipo ({@link CatalogoFabricas#ORDEM}) e depois por id.
	 * Ciclos = soma das eficiências x ciclos base (fração permitida); limitados pelos insumos disponíveis.
	 * Insumos são debitados antes do crédito dos produtos.
	 */
	@Transactional
	public void processarProducaoFabricas(Vila vila, int turno) {
		List<Construcao> fabricas = construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.ATIVA)
				.stream().filter(c -> CatalogoFabricas.ehFabrica(c.getTipo()))
				.sorted(Comparator.<Construcao>comparingInt(c -> CatalogoFabricas.ORDEM.indexOf(c.getTipo()))
						.thenComparing(Construcao::getId))
				.toList();
		for (Construcao c : fabricas) {
			int bonus = bonusTerrenoService.bonusDoPredio(vila.getId(), c);
			BigDecimal fator = fator(bonus);
			List<Trabalhador> trabalhadores = consultaTrabalhadores.trabalhadores(c, vila);
			double somaEficiencia = trabalhadores.stream().mapToDouble(Trabalhador::eficiencia).sum();
			if (somaEficiencia <= 0) {
				continue;
			}
			Map<Recurso, BigDecimal> consumido = new LinkedHashMap<>();
			Map<Recurso, BigDecimal> produzido = new LinkedHashMap<>();
			BigDecimal totalCiclos = BigDecimal.ZERO;
			BigDecimal restante = null;
			for (Receita rc : CatalogoFabricas.receitas(c.getTipo(), c.getNivel(), c.getConfiguracao())) {
				BigDecimal disponivel = BigDecimal.valueOf(somaEficiencia * rc.ciclosBase()).multiply(fator)
						.setScale(2, RoundingMode.HALF_UP);
				if (restante == null) {
					restante = disponivel;
				} else {
					// alternativas compartilham o mesmo limite de ciclos (proporcional ao ciclo base igual)
					disponivel = disponivel.min(restante);
				}
				BigDecimal ciclos = disponivel;
				for (Map.Entry<Recurso, Integer> i : rc.insumos().entrySet()) {
					BigDecimal possiveis = estoqueService.quantidade(vila, i.getKey())
							.divide(BigDecimal.valueOf(i.getValue()), 2, RoundingMode.DOWN);
					ciclos = ciclos.min(possiveis);
				}
				if (ciclos.signum() <= 0) {
					continue;
				}
				for (Map.Entry<Recurso, Integer> i : rc.insumos().entrySet()) {
					BigDecimal q = ciclos.multiply(BigDecimal.valueOf(i.getValue()));
					estoqueService.debitar(vila, Map.of(i.getKey(), q));
					consumido.merge(i.getKey(), q, BigDecimal::add);
				}
				for (Map.Entry<Recurso, Integer> p : rc.produtos().entrySet()) {
					produzido.merge(p.getKey(), ciclos.multiply(BigDecimal.valueOf(p.getValue())), BigDecimal::add);
				}
				totalCiclos = totalCiclos.add(ciclos);
				restante = restante.subtract(ciclos);
			}
			if (produzido.isEmpty()) {
				continue;
			}
			registrarProducao(vila, turno, c, CATEGORIA_FABRICA, trabalhadores.size(), produzido, consumido,
					totalCiclos, bonus);
		}
	}

	private void registrarProducao(Vila vila, int turno, Construcao c, String categoria, int produtivos,
			Map<Recurso, BigDecimal> produzido, Map<Recurso, BigDecimal> consumido, BigDecimal ciclos, int bonus) {
		Map<String, Object> extras = new LinkedHashMap<>();
		extras.put("consumido", consumido.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(),
				e -> e.getValue().toPlainString(), (a, b) -> a, LinkedHashMap::new)));
		extras.put("ciclos", ciclos.toPlainString());
		if (bonus > 0) {
			extras.put("bonusTerreno", bonus);
		}
		registrarProducao(vila, turno, c, categoria, produtivos, produzido, extras);
	}

	private static BigDecimal fator(int bonus) {
		return BigDecimal.ONE.add(BigDecimal.valueOf(bonus).movePointLeft(2));
	}

	/** Credita o produzido ao estoque e registra o evento PRODUCAO do prédio (se houver produção). */
	@Transactional
	public void registrarProducao(Vila vila, int turno, Construcao c, String categoria, int produtivos,
			Map<Recurso, BigDecimal> produzido) {
		registrarProducao(vila, turno, c, categoria, produtivos, produzido, Map.of());
	}

	private void registrarProducao(Vila vila, int turno, Construcao c, String categoria, int produtivos,
			Map<Recurso, BigDecimal> produzido, Map<String, Object> extras) {
		if (produzido.isEmpty()) {
			return;
		}
		produzido.forEach((r, q) -> estoqueService.creditar(vila, r, q));
		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("categoria", categoria);
		dados.put("construcaoId", c.getId());
		dados.put("tipo", c.getTipo().name());
		dados.put("nivel", c.getNivel().name());
		dados.put("trabalhadoresProdutivos", produtivos);
		dados.put("produzido", produzido.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().toPlainString(),
						(a, b) -> a, LinkedHashMap::new)));
		dados.putAll(extras);
		String texto = produzido.entrySet().stream()
				.map(e -> e.getValue().stripTrailingZeros().toPlainString() + " " + e.getKey().getNomeExibicao())
				.collect(Collectors.joining(", "));
		registro.registrar(vila, turno, TipoEventoTurno.PRODUCAO,
				"%s produziu %s".formatted(ConstrucaoCatalogo.de(c.getTipo()).nome(), texto), dados);
	}

	private static List<Trabalhador> selecionarProdutivos(List<Trabalhador> trabalhadores, int marcados) {
		int limite = Math.min(trabalhadores.size(), marcados / 2);
		List<Trabalhador> ordenados = new ArrayList<>(trabalhadores);
		ordenados.sort(Comparator.comparingDouble(Trabalhador::eficiencia).reversed());
		return ordenados.subList(0, limite);
	}

	/** Marcações do prédio cujo ladrilho tem o terreno do prédio. */
	private int marcadosValidos(Vila vila, Construcao c) {
		List<ConstrucaoMarcacao> marcacoes = marcacaoRepository.findByConstrucaoId(c.getId());
		if (marcacoes.isEmpty()) {
			return 0;
		}
		TipoTerreno esperado = ConstrucaoCatalogo.terreno(c.getTipo()).orElse(null);
		if (esperado == null) {
			return 0;
		}
		Set<String> corretos = regiaoRepository.findByVilaIdAndIndice(vila.getId(), c.getRegiaoIndice())
				.map(r -> ladrilhoRepository.findByRegiaoIdAndTerrenoOrderByYAscXAsc(r.getId(), esperado).stream()
						.map(l -> l.getX() + "," + l.getY()).collect(Collectors.toSet()))
				.orElse(Set.of());
		return (int) marcacoes.stream().filter(m -> corretos.contains(m.getX() + "," + m.getY())).count();
	}

}
