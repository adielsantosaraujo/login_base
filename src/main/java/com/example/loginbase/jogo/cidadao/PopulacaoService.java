package com.example.loginbase.jogo.cidadao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.servico.VilaAtual;

/** Consulta e confirmação da distribuição inicial de pontos e da família líder. */
@Service
public class PopulacaoService {

	static final int MAX_POR_CARACTERISTICA = 10;
	static final int MAX_TOTAL_CARACTERISTICAS = 20;
	static final int MAX_POR_PROFISSAO = 5;
	static final int MAX_TOTAL_PROFISSOES = 10;

	private final VilaAtual vilaAtual;
	private final VilaRepository vilaRepository;
	private final FamiliaRepository familiaRepository;
	private final CidadaoRepository cidadaoRepository;
	private final CidadaoProfissaoRepository profissaoRepository;

	public PopulacaoService(VilaAtual vilaAtual, VilaRepository vilaRepository, FamiliaRepository familiaRepository,
			CidadaoRepository cidadaoRepository, CidadaoProfissaoRepository profissaoRepository) {
		this.vilaAtual = vilaAtual;
		this.vilaRepository = vilaRepository;
		this.familiaRepository = familiaRepository;
		this.cidadaoRepository = cidadaoRepository;
		this.profissaoRepository = profissaoRepository;
	}

	@Transactional(readOnly = true)
	public PopulacaoDTO obter() {
		return montar(vilaAtual.obter());
	}

	@Transactional
	public PopulacaoDTO confirmar(DistribuirPopulacaoRequest req) {
		Vila vila = vilaAtual.obter();
		if (vila.isPopulacaoConfirmada()) {
			throw invalido("A população já foi confirmada");
		}
		if (req == null || req.familiaLiderId() == null) {
			throw invalido("Escolha uma família líder");
		}
		List<Familia> familias = familiaRepository.findByVilaId(vila.getId());
		if (familias.stream().noneMatch(f -> f.getId().equals(req.familiaLiderId()))) {
			throw invalido("Família líder inválida");
		}
		Map<Long, Cidadao> cidadaos = new LinkedHashMap<>();
		cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId()).forEach(c -> cidadaos.put(c.getId(), c));
		Set<Long> vistos = new HashSet<>();
		List<DistribuirPopulacaoRequest.DistribuicaoCidadao> itens = req.cidadaos() == null ? List.of()
				: req.cidadaos();
		for (var item : itens) {
			Cidadao c = item == null || item.cidadaoId() == null ? null : cidadaos.get(item.cidadaoId());
			if (c == null) {
				throw invalido("Cidadão inválido: " + (item == null ? null : item.cidadaoId()));
			}
			if (!vistos.add(c.getId())) {
				throw invalido("Cidadão repetido: " + c.getNome());
			}
			aplicar(c, item);
		}
		vila.setFamiliaLiderId(req.familiaLiderId());
		vila.setPopulacaoConfirmada(true);
		vilaRepository.save(vila);
		return montar(vila);
	}

	private void aplicar(Cidadao c, DistribuirPopulacaoRequest.DistribuicaoCidadao item) {
		Map<Caracteristica, Integer> car = new EnumMap<>(Caracteristica.class);
		int totalCar = 0;
		for (var e : mapa(item.caracteristicas()).entrySet()) {
			Caracteristica k = parse(Caracteristica.class, e.getKey(), "Característica inválida: ");
			int v = valor(e.getValue(), c);
			if (v > MAX_POR_CARACTERISTICA) {
				throw invalido("Máximo 10 por característica");
			}
			car.merge(k, v, Integer::sum);
			totalCar += v;
		}
		if (totalCar > MAX_TOTAL_CARACTERISTICAS || totalCar > c.getPontosCarPendentes()) {
			throw invalido("Máximo 20 pontos de característica");
		}
		Map<Profissao, Integer> prof = new EnumMap<>(Profissao.class);
		int totalProf = 0;
		for (var e : mapa(item.profissoes()).entrySet()) {
			Profissao k = parse(Profissao.class, e.getKey(), "Profissão inválida: ");
			int v = valor(e.getValue(), c);
			if (v > MAX_POR_PROFISSAO) {
				throw invalido("Máximo 5 por profissão");
			}
			prof.merge(k, v, Integer::sum);
			totalProf += v;
		}
		if (totalProf > MAX_TOTAL_PROFISSOES || totalProf > c.getPontosProfPendentes()) {
			throw invalido("Máximo 10 pontos de profissão");
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
		c.setPontosCarPendentes(c.getPontosCarPendentes() - totalCar);
		c.setPontosProfPendentes(c.getPontosProfPendentes() - totalProf);
		cidadaoRepository.save(c);
		prof.forEach((k, v) -> {
			if (v > 0) {
				CidadaoProfissao cp = profissaoRepository.findByCidadaoIdAndProfissao(c.getId(), k)
						.orElseGet(() -> new CidadaoProfissao(c.getId(), k, 0));
				cp.setPontosBase(cp.getPontosBase() + v);
				profissaoRepository.save(cp);
			}
		});
	}

	private static Map<String, Integer> mapa(Map<String, Integer> m) {
		return m == null ? Map.of() : m;
	}

	private static int valor(Integer v, Cidadao c) {
		if (v == null || v < 0) {
			throw invalido("Pontos inválidos para " + c.getNome());
		}
		return v;
	}

	private static <E extends Enum<E>> E parse(Class<E> tipo, String nome, String msg) {
		try {
			return Enum.valueOf(tipo, nome == null ? "" : nome.toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			throw invalido(msg + nome);
		}
	}

	private PopulacaoDTO montar(Vila vila) {
		List<PopulacaoDTO.FamiliaPop> out = new ArrayList<>();
		List<Cidadao> vivos = cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId());
		for (Familia f : familiaRepository.findByVilaId(vila.getId()).stream()
				.sorted(Comparator.comparing(Familia::getId)).toList()) {
			List<PopulacaoDTO.CidadaoPop> membros = vivos.stream().filter(c -> f.getId().equals(c.getFamiliaId()))
					.sorted(Comparator.comparing(Cidadao::getIdadeMeses).reversed().thenComparing(Cidadao::getId))
					.map(this::dto).toList();
			out.add(new PopulacaoDTO.FamiliaPop(f.getId(), f.getSobrenome(), membros));
		}
		return new PopulacaoDTO(vila.isPopulacaoConfirmada(), vila.getFamiliaLiderId(), out);
	}

	private PopulacaoDTO.CidadaoPop dto(Cidadao c) {
		Map<String, Integer> car = new LinkedHashMap<>();
		for (Caracteristica k : Caracteristica.values()) {
			car.put(k.name(), c.valorCaracteristica(k));
		}
		Map<String, Integer> prof = new LinkedHashMap<>();
		profissaoRepository.findByCidadaoId(c.getId()).stream()
				.sorted(Comparator.comparing(CidadaoProfissao::getProfissao))
				.forEach(p -> prof.put(p.getProfissao().name(), p.getPontosBase()));
		return new PopulacaoDTO.CidadaoPop(c.getId(), c.getNome(), c.getSexo(), c.getIdadeAnos(), car,
				c.getPontosCarPendentes(), c.getPontosProfPendentes(), prof);
	}

	private static JogoException invalido(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}

}
