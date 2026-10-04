package com.example.loginbase.jogo.cidadao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.CodigoErro;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.servico.VilaAtual;

/** Consulta e confirmação da distribuição inicial de pontos e da família líder. */
@Service
public class PopulacaoService {

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
	public ConfirmacaoPopulacaoDTO confirmar(DistribuirPopulacaoRequest req) {
		Vila vila = vilaAtual.obter();
		if (vila.isPopulacaoConfirmada()) {
			throw new JogoException(HttpStatus.CONFLICT, CodigoErro.POPULACAO_JA_CONFIRMADA,
					"A população já foi confirmada");
		}
		List<Familia> familias = familiaRepository.findByVilaId(vila.getId());
		Map<Long, Cidadao> vivos = new LinkedHashMap<>();
		cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId()).forEach(c -> vivos.put(c.getId(), c));

		// POPULACAO_INCOMPLETA: cada cidadão vivo exatamente uma vez, na família informada
		Map<Long, Entrada> entradas = new LinkedHashMap<>();
		List<DistribuirPopulacaoRequest.FamiliaEntradaDTO> fams = req == null || req.familias() == null ? List.of()
				: req.familias();
		boolean ok = true;
		for (var f : fams) {
			if (f == null || f.familiaId() == null || f.cidadaos() == null) {
				ok = false;
				continue;
			}
			for (var item : f.cidadaos()) {
				Cidadao c = item == null || item.cidadaoId() == null ? null : vivos.get(item.cidadaoId());
				if (c == null || !f.familiaId().equals(c.getFamiliaId())
						|| entradas.put(c.getId(), new Entrada(c, item)) != null) {
					ok = false;
				}
			}
		}
		if (!ok || entradas.size() != vivos.size()) {
			throw erro(CodigoErro.POPULACAO_INCOMPLETA, "Informe os " + DistribuicaoPopulacao.POPULACAO
					+ " cidadãos da vila");
		}

		// PONTOS_INVALIDOS: normaliza chaves e valida valores
		for (Entrada e : entradas.values()) {
			e.car = new EnumMap<>(Caracteristica.class);
			e.prof = new EnumMap<>(Profissao.class);
			converter(e.item.caracteristicas(), Caracteristica.class, e.car, e.cidadao);
			converter(e.item.profissoes(), Profissao.class, e.prof, e.cidadao);
		}
		for (Entrada e : entradas.values()) {
			if (soma(e.car) > DistribuicaoPopulacao.LIMITE_CARACTERISTICAS) {
				throw erro(CodigoErro.LIMITE_CARACTERISTICAS, "Máximo " + DistribuicaoPopulacao.LIMITE_CARACTERISTICAS
						+ " pontos de característica (" + e.cidadao.getNome() + ")");
			}
		}
		for (Entrada e : entradas.values()) {
			if (soma(e.prof) > DistribuicaoPopulacao.LIMITE_PROFISSOES) {
				throw erro(CodigoErro.LIMITE_PROFISSOES, "Máximo " + DistribuicaoPopulacao.LIMITE_PROFISSOES
						+ " pontos de profissão (" + e.cidadao.getNome() + ")");
			}
		}
		Long liderId = req.familiaLiderId();
		if (liderId == null || familias.stream().noneMatch(f -> f.getId().equals(liderId))) {
			throw erro(CodigoErro.FAMILIA_LIDER_OBRIGATORIA, "Escolha uma família líder");
		}
		int construtores = 0;
		int carregadores = 0;
		for (Entrada e : entradas.values()) {
			Profissao pr = DistribuicaoPopulacao.principal(e.prof);
			if (pr == Profissao.CONSTRUTOR) {
				construtores++;
			}
			else if (pr == Profissao.CARREGADOR) {
				carregadores++;
			}
		}
		if (construtores < DistribuicaoPopulacao.MINIMOS.get(Profissao.CONSTRUTOR)) {
			throw erro(CodigoErro.MINIMO_CONSTRUTORES, "São necessários ao menos "
					+ DistribuicaoPopulacao.MINIMOS.get(Profissao.CONSTRUTOR) + " Construtores principais");
		}
		if (carregadores < DistribuicaoPopulacao.MINIMOS.get(Profissao.CARREGADOR)) {
			throw erro(CodigoErro.MINIMO_CARREGADORES, "São necessários ao menos "
					+ DistribuicaoPopulacao.MINIMOS.get(Profissao.CARREGADOR) + " Carregadores principais");
		}

		// Gravação
		for (Entrada e : entradas.values()) {
			Cidadao c = e.cidadao;
			c.setVit(e.car.getOrDefault(Caracteristica.VIT, 0));
			c.setForca(e.car.getOrDefault(Caracteristica.FOR, 0));
			c.setVel(e.car.getOrDefault(Caracteristica.VEL, 0));
			c.setInteligencia(e.car.getOrDefault(Caracteristica.INT, 0));
			c.setCar(e.car.getOrDefault(Caracteristica.CAR, 0));
			c.setPontosCarPendentes(DistribuicaoPopulacao.LIMITE_CARACTERISTICAS - soma(e.car));
			c.setPontosProfPendentes(DistribuicaoPopulacao.LIMITE_PROFISSOES - soma(e.prof));
			cidadaoRepository.save(c);
			for (Profissao p : Profissao.values()) {
				int v = e.prof.getOrDefault(p, 0);
				var existente = profissaoRepository.findByCidadaoIdAndProfissao(c.getId(), p);
				if (v > 0) {
					CidadaoProfissao cp = existente.orElseGet(() -> new CidadaoProfissao(c.getId(), p, 0));
					cp.setPontosBase(v);
					profissaoRepository.save(cp);
				}
				else {
					existente.ifPresent(profissaoRepository::delete);
				}
			}
		}
		vila.setFamiliaLiderId(liderId);
		vila.setPopulacaoConfirmada(true);
		vilaRepository.save(vila);

		// Bônus do líder: adulto mais velho da família líder
		List<Entrada> membros = entradas.values().stream().filter(e -> liderId.equals(e.cidadao.getFamiliaId()))
				.toList();
		var familiaEntrada = new DistribuicaoPopulacao.FamiliaEntrada(membros.stream()
				.map(e -> new DistribuicaoPopulacao.PessoaEntrada(e.cidadao.getNome(), e.cidadao.getSexo(),
						e.cidadao.getIdadeAnos(), papel(e.cidadao)))
				.toList());
		int idx = DistribuicaoPopulacao.liderDaFamilia(familiaEntrada);
		int bonus = idx < 0 ? 0
				: DistribuicaoPopulacao.bonusLider(membros.get(idx).car.getOrDefault(Caracteristica.CAR, 0));
		return new ConfirmacaoPopulacaoDTO(bonus, liderId, "MAPA");
	}

	private static final class Entrada {

		final Cidadao cidadao;
		final DistribuirPopulacaoRequest.CidadaoEntradaDTO item;
		EnumMap<Caracteristica, Integer> car;
		EnumMap<Profissao, Integer> prof;

		Entrada(Cidadao cidadao, DistribuirPopulacaoRequest.CidadaoEntradaDTO item) {
			this.cidadao = cidadao;
			this.item = item;
		}

	}

	private static <E extends Enum<E>> void converter(Map<String, Integer> origem, Class<E> tipo,
			EnumMap<E, Integer> destino, Cidadao c) {
		if (origem == null) {
			return;
		}
		for (var e : origem.entrySet()) {
			E k;
			try {
				k = Enum.valueOf(tipo, e.getKey() == null ? "" : e.getKey().toUpperCase());
			}
			catch (IllegalArgumentException ex) {
				throw erro(CodigoErro.PONTOS_INVALIDOS, "Pontos inválidos para " + c.getNome());
			}
			Integer v = e.getValue();
			if (v == null || v < 0) {
				throw erro(CodigoErro.PONTOS_INVALIDOS, "Pontos inválidos para " + c.getNome());
			}
			destino.merge(k, v, Integer::sum);
		}
	}

	private static int soma(Map<?, Integer> m) {
		return m.values().stream().mapToInt(Integer::intValue).sum();
	}

	private PopulacaoDTO montar(Vila vila) {
		List<Familia> familias = familiaRepository.findByVilaId(vila.getId()).stream()
				.sorted(Comparator.comparing(Familia::getId)).toList();
		List<Cidadao> vivos = cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId());
		boolean confirmada = vila.isPopulacaoConfirmada();

		List<List<Cidadao>> membros = new ArrayList<>();
		List<DistribuicaoPopulacao.FamiliaEntrada> entradas = new ArrayList<>();
		for (Familia f : familias) {
			List<Cidadao> lista = vivos.stream().filter(c -> f.getId().equals(c.getFamiliaId()))
					.sorted(Comparator.comparing((Cidadao c) -> papel(c)).thenComparing(Cidadao::getId)).toList();
			membros.add(lista);
			entradas.add(new DistribuicaoPopulacao.FamiliaEntrada(lista.stream()
					.map(c -> new DistribuicaoPopulacao.PessoaEntrada(c.getNome(), c.getSexo(), c.getIdadeAnos(),
							papel(c)))
					.toList()));
		}

		List<List<DistribuicaoPopulacao.DistribuicaoPessoa>> pontos = new ArrayList<>();
		for (int i = 0; i < familias.size(); i++) {
			pontos.add(new ArrayList<>());
		}
		if (confirmada) {
			for (int i = 0; i < familias.size(); i++) {
				for (Cidadao c : membros.get(i)) {
					pontos.get(i).add(gravado(c));
				}
			}
		}
		else if (!familias.isEmpty()) {
			pontos = DistribuicaoPopulacao.distribuirPopulacao(entradas, DistribuicaoPopulacao.PLANO_PADRAO);
		}

		Long sugerida = null;
		if (!familias.isEmpty() && pontos.stream().noneMatch(l -> l.contains(null))) {
			sugerida = familias.get(DistribuicaoPopulacao.familiaLiderSugerida(entradas, pontos)).getId();
		}

		List<PopulacaoDTO.FamiliaDTO> out = new ArrayList<>();
		for (int i = 0; i < familias.size(); i++) {
			List<PopulacaoDTO.CidadaoDTO> cids = new ArrayList<>();
			for (int m = 0; m < membros.get(i).size(); m++) {
				Cidadao c = membros.get(i).get(m);
				DistribuicaoPopulacao.DistribuicaoPessoa d = pontos.get(i).get(m);
				LinkedHashMap<Caracteristica, Integer> car = new LinkedHashMap<>();
				for (Caracteristica k : Caracteristica.values()) {
					car.put(k, d.caracteristicas().getOrDefault(k, 0));
				}
				LinkedHashMap<Profissao, Integer> prof = new LinkedHashMap<>();
				for (Profissao k : Profissao.values()) {
					prof.put(k, d.profissoes().getOrDefault(k, 0));
				}
				cids.add(new PopulacaoDTO.CidadaoDTO(c.getId(), c.getNome(), c.getSexo(), c.getIdadeAnos(), papel(c),
						car, prof));
			}
			out.add(new PopulacaoDTO.FamiliaDTO(familias.get(i).getId(), familias.get(i).getSobrenome(), cids));
		}

		LinkedHashMap<Profissao, Integer> plano = new LinkedHashMap<>();
		for (Profissao p : DistribuicaoPopulacao.ORDEM_PLANO) {
			plano.put(p, DistribuicaoPopulacao.PLANO_PADRAO.getOrDefault(p, 0));
		}
		LinkedHashMap<Profissao, Integer> minimos = new LinkedHashMap<>();
		minimos.put(Profissao.CONSTRUTOR, DistribuicaoPopulacao.MINIMOS.get(Profissao.CONSTRUTOR));
		minimos.put(Profissao.CARREGADOR, DistribuicaoPopulacao.MINIMOS.get(Profissao.CARREGADOR));
		return new PopulacaoDTO(confirmada, plano, minimos,
				new PopulacaoDTO.LimitesDTO(DistribuicaoPopulacao.LIMITE_CARACTERISTICAS,
						DistribuicaoPopulacao.LIMITE_PROFISSOES),
				sugerida, vila.getFamiliaLiderId(), out);
	}

	/** Papel derivado: sem pai nem mãe => PAI/MAE; senão FILHO/FILHA. */
	static PapelFamiliar papel(Cidadao c) {
		boolean masculino = c.getSexo() == Sexo.M;
		if (c.getPaiId() == null && c.getMaeId() == null) {
			return masculino ? PapelFamiliar.PAI : PapelFamiliar.MAE;
		}
		return masculino ? PapelFamiliar.FILHO : PapelFamiliar.FILHA;
	}

	private DistribuicaoPopulacao.DistribuicaoPessoa gravado(Cidadao c) {
		EnumMap<Caracteristica, Integer> car = new EnumMap<>(Caracteristica.class);
		for (Caracteristica k : Caracteristica.values()) {
			car.put(k, c.valorCaracteristica(k));
		}
		EnumMap<Profissao, Integer> prof = new EnumMap<>(Profissao.class);
		for (Profissao k : Profissao.values()) {
			prof.put(k, 0);
		}
		profissaoRepository.findByCidadaoId(c.getId()).forEach(p -> prof.put(p.getProfissao(), p.getPontosBase()));
		return new DistribuicaoPopulacao.DistribuicaoPessoa(car, prof);
	}

	private static JogoException erro(String codigo, String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, codigo, msg);
	}

}
