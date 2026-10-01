package com.example.loginbase.jogo.cidadao;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.Vila;

@Service
public class CidadaoService {

	static final int IDADE_TRABALHO_MIN = 14;
	static final int IDADE_TRABALHO_MAX = 64;

	private final CidadaoRepository cidadaoRepository;
	private final CidadaoProfissaoRepository cidadaoProfissaoRepository;
	private final FamiliaRepository familiaRepository;
	private final CalculadoraPeEfetivo calculadoraPeEfetivo;
	private final EficienciaService eficienciaService;

	public CidadaoService(CidadaoRepository cidadaoRepository, CidadaoProfissaoRepository cidadaoProfissaoRepository,
			FamiliaRepository familiaRepository, CalculadoraPeEfetivo calculadoraPeEfetivo,
			EficienciaService eficienciaService) {
		this.cidadaoRepository = cidadaoRepository;
		this.cidadaoProfissaoRepository = cidadaoProfissaoRepository;
		this.familiaRepository = familiaRepository;
		this.calculadoraPeEfetivo = calculadoraPeEfetivo;
		this.eficienciaService = eficienciaService;
	}

	@Transactional(readOnly = true)
	public CidadaoDTO obter(Vila vila, Long id) {
		return montar(vila, buscar(vila, id));
	}

	@Transactional(readOnly = true)
	public List<CidadaoResumoDTO> listar(Vila vila, boolean elegiveisTrabalho) {
		return cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId()).stream()
				.filter(c -> !elegiveisTrabalho || (c.getIdadeAnos() >= IDADE_TRABALHO_MIN
						&& c.getIdadeAnos() <= IDADE_TRABALHO_MAX && c.getTropaId() == null
						&& c.getConstrucaoId() == null))
				.map(CidadaoResumoDTO::de).toList();
	}

	/** Gasta pontos pendentes (soma pedida ≤ pendentes; o restante continua pendente). */
	@Transactional
	public CidadaoDTO distribuirPontos(Vila vila, Long id, DistribuirPontosRequest req) {
		Cidadao c = buscar(vila, id);
		if (!c.isVivo()) {
			throw erro("Cidadão morto não pode receber pontos");
		}
		Map<Caracteristica, Integer> car = new EnumMap<>(Caracteristica.class);
		Map<Profissao, Integer> prof = new EnumMap<>(Profissao.class);
		if (req != null) {
			if (req.caracteristicas() != null) {
				req.caracteristicas().forEach((k, v) -> car.merge(parse(Caracteristica.class, k, "característica"),
						valor(v), Integer::sum));
			}
			if (req.profissoes() != null) {
				req.profissoes().forEach((k, v) -> prof.merge(parse(Profissao.class, k, "profissão"), valor(v),
						Integer::sum));
			}
		}
		int somaCar = car.values().stream().mapToInt(Integer::intValue).sum();
		int somaProf = prof.values().stream().mapToInt(Integer::intValue).sum();
		if (somaCar == 0 && somaProf == 0) {
			throw erro("Nenhum ponto informado");
		}
		if (somaCar > c.getPontosCarPendentes()) {
			throw erro("Pontos de característica acima do disponível (" + c.getPontosCarPendentes() + ")");
		}
		if (somaProf > c.getPontosProfPendentes()) {
			throw erro("Pontos de profissão acima do disponível (" + c.getPontosProfPendentes() + ")");
		}
		car.forEach((k, v) -> {
			switch (k) {
				case VIT -> c.setVit(c.getVit() + v);
				case FOR -> c.setForca(c.getForca() + v);
				case VEL -> c.setVel(c.getVel() + v);
				case INT -> c.setInteligencia(c.getInteligencia() + v);
				case CAR -> c.setCar(c.getCar() + v);
			}
		});
		c.setPontosCarPendentes(c.getPontosCarPendentes() - somaCar);
		c.setPontosProfPendentes(c.getPontosProfPendentes() - somaProf);
		prof.forEach((p, v) -> {
			if (v > 0) {
				CidadaoProfissao cp = cidadaoProfissaoRepository.findByCidadaoIdAndProfissao(c.getId(), p)
						.orElseGet(() -> new CidadaoProfissao(c.getId(), p, 0));
				cp.setPontosBase(cp.getPontosBase() + v);
				cidadaoProfissaoRepository.save(cp);
			}
		});
		cidadaoRepository.saveAndFlush(c);
		return montar(vila, c);
	}

	private CidadaoDTO montar(Vila vila, Cidadao c) {
		Map<String, Integer> car = new LinkedHashMap<>();
		for (Caracteristica k : Caracteristica.values()) {
			car.put(k.name(), c.valorCaracteristica(k));
		}
		Map<Profissao, Integer> bases = new EnumMap<>(Profissao.class);
		cidadaoProfissaoRepository.findByCidadaoId(c.getId()).forEach(p -> bases.put(p.getProfissao(), p.getPontosBase()));
		List<CidadaoDTO.ProfissaoDTO> profissoes = java.util.Arrays.stream(Profissao.values()).map(p -> {
			int base = bases.getOrDefault(p, 0);
			return new CidadaoDTO.ProfissaoDTO(p, base, calculadoraPeEfetivo.calcular(c, p, base),
					eficienciaService.eficiencia(c, p, vila));
		}).toList();
		String familiaNome = c.getFamiliaId() == null ? null
				: familiaRepository.findById(c.getFamiliaId()).map(Familia::getSobrenome).orElse(null);
		CidadaoDTO.Conjuge conjuge = c.getConjugeId() == null ? null
				: cidadaoRepository.findById(c.getConjugeId()).map(x -> new CidadaoDTO.Conjuge(x.getId(), x.getNome()))
						.orElse(null);
		return new CidadaoDTO(c.getId(), c.getNome(), c.getSexo(), c.getIdadeAnos(), c.isVivo(), c.getEstado(),
				c.getFamintoTurnos() > 0, c.getFamiliaId(), familiaNome, conjuge, car, c.getPontosCarPendentes(),
				c.getPontosProfPendentes(), profissoes, c.getConstrucaoId(), c.getProfissaoTrabalho(), Map.of());
	}

	private Cidadao buscar(Vila vila, Long id) {
		Cidadao c = cidadaoRepository.findById(id)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Cidadão não encontrado"));
		if (!c.getVilaId().equals(vila.getId())) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Cidadão pertence a outra vila");
		}
		return c;
	}

	private static int valor(Integer v) {
		if (v == null || v < 0) {
			throw erro("Quantidade de pontos inválida");
		}
		return v;
	}

	private static <E extends Enum<E>> E parse(Class<E> tipo, String chave, String rotulo) {
		try {
			return Enum.valueOf(tipo, chave.trim().toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw erro("Nome de " + rotulo + " inválido: " + chave);
		}
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}

}
