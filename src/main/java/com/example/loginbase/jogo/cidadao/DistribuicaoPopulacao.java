package com.example.loginbase.jogo.cidadao;

import static com.example.loginbase.jogo.cidadao.Caracteristica.CAR;
import static com.example.loginbase.jogo.cidadao.Profissao.*;

import com.example.loginbase.jogo.comum.CodigoErro;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Distribuição automática da população inicial (porte de distribuicao-populacao.js).
 * Puro e determinístico, sem estado.
 */
public final class DistribuicaoPopulacao {

	public static final int POPULACAO = 16;
	public static final int LIMITE_CARACTERISTICAS = 20;
	public static final int LIMITE_PROFISSOES = 10;

	public static final Map<Profissao, Integer> PLANO_PADRAO = planoPadrao();
	public static final Map<Profissao, Integer> MINIMOS = new EnumMap<>(Map.of(CONSTRUTOR, 2, CARREGADOR, 2));

	static final List<Profissao> ORDEM_PLANO = List.of(COMERCIANTE, CONSTRUTOR, CARREGADOR, MADEIREIRO, MINEIRO,
			AGRICULTOR, FAZENDEIRO, COZINHEIRO, GUERREIRO, FERREIRO, COSTUREIRO, CACADOR);

	private static final Map<Profissao, Profissao> SECUNDARIA = new EnumMap<>(Map.ofEntries(
			Map.entry(CONSTRUTOR, CARREGADOR), Map.entry(CARREGADOR, CONSTRUTOR),
			Map.entry(AGRICULTOR, FAZENDEIRO), Map.entry(FAZENDEIRO, AGRICULTOR),
			Map.entry(MINEIRO, MADEIREIRO), Map.entry(MADEIREIRO, MINEIRO),
			Map.entry(FERREIRO, MINEIRO), Map.entry(COZINHEIRO, COMERCIANTE),
			Map.entry(COSTUREIRO, COZINHEIRO), Map.entry(CACADOR, GUERREIRO),
			Map.entry(GUERREIRO, CACADOR), Map.entry(COMERCIANTE, COZINHEIRO)));

	private static final List<Profissao> APOIO_CANDIDATAS = List.of(CARREGADOR, CONSTRUTOR, MADEIREIRO);

	private static final int[] PONTOS_PROF = { 5, 3, 2 };
	private static final double PESO_BASE_VIT = 0.5;
	private static final double PESO_PRINCIPAL = 3;
	private static final double PESO_SECUNDARIA = 1.5;
	private static final double PESO_APOIO = 0.5;

	public record PessoaEntrada(String nome, Sexo sexo, int idadeAnos, PapelFamiliar papel) {
	}

	public record FamiliaEntrada(List<PessoaEntrada> membros) {
	}

	public record DistribuicaoPessoa(EnumMap<Caracteristica, Integer> caracteristicas,
			EnumMap<Profissao, Integer> profissoes) {
	}

	private DistribuicaoPopulacao() {
	}

	private static Map<Profissao, Integer> planoPadrao() {
		EnumMap<Profissao, Integer> p = new EnumMap<>(Profissao.class);
		for (Profissao x : Profissao.values()) {
			p.put(x, 0);
		}
		p.put(COMERCIANTE, 1);
		p.put(CONSTRUTOR, 2);
		p.put(CARREGADOR, 2);
		p.put(MADEIREIRO, 2);
		p.put(MINEIRO, 2);
		p.put(AGRICULTOR, 2);
		p.put(FAZENDEIRO, 1);
		p.put(COZINHEIRO, 1);
		p.put(GUERREIRO, 2);
		p.put(FERREIRO, 1);
		return java.util.Collections.unmodifiableMap(p);
	}

	/** Distribui os pontos de uma pessoa para a profissão principal. */
	public static DistribuicaoPessoa distribuirPessoa(Profissao principal) {
		Profissao sec = SECUNDARIA.get(principal);
		Profissao apoio = APOIO_CANDIDATAS.stream().filter(k -> k != principal && k != sec).findFirst().orElseThrow();

		EnumMap<Profissao, Integer> profissoes = new EnumMap<>(Profissao.class);
		for (Profissao p : Profissao.values()) {
			profissoes.put(p, 0);
		}
		profissoes.put(principal, PONTOS_PROF[0]);
		profissoes.put(sec, PONTOS_PROF[1]);
		profissoes.put(apoio, PONTOS_PROF[2]);

		EnumMap<Caracteristica, Double> peso = new EnumMap<>(Caracteristica.class);
		for (Caracteristica c : Caracteristica.values()) {
			peso.put(c, 0.0);
		}
		peso.put(Caracteristica.VIT, PESO_BASE_VIT);
		principal.getCaracteristicas().forEach(c -> peso.merge(c, PESO_PRINCIPAL, Double::sum));
		sec.getCaracteristicas().forEach(c -> peso.merge(c, PESO_SECUNDARIA, Double::sum));
		apoio.getCaracteristicas().forEach(c -> peso.merge(c, PESO_APOIO, Double::sum));

		EnumMap<Caracteristica, Integer> caracteristicas = new EnumMap<>(Caracteristica.class);
		for (Caracteristica c : Caracteristica.values()) {
			caracteristicas.put(c, 0);
		}
		for (int i = 0; i < LIMITE_CARACTERISTICAS; i++) {
			Caracteristica melhor = null;
			double score = -1;
			for (Caracteristica c : Caracteristica.values()) {
				double s = peso.get(c) / (caracteristicas.get(c) + 1);
				if (s > score) {
					score = s;
					melhor = c;
				}
			}
			caracteristicas.merge(melhor, 1, Integer::sum);
		}
		return new DistribuicaoPessoa(caracteristicas, profissoes);
	}

	/**
	 * Distribui a população conforme o plano (soma 16). Rodízio: pais, mães, filhos, filhas, percorrendo
	 * as famílias. Retorna, por família, as distribuições na ordem dos membros informados.
	 */
	public static List<List<DistribuicaoPessoa>> distribuirPopulacao(List<FamiliaEntrada> familias,
			Map<Profissao, Integer> plano) {
		int total = plano.values().stream().mapToInt(Integer::intValue).sum();
		if (total != POPULACAO) {
			throw new IllegalArgumentException("Plano deve somar " + POPULACAO);
		}
		List<Profissao> papeis = new ArrayList<>();
		for (Profissao p : ORDEM_PLANO) {
			for (int i = 0; i < plano.getOrDefault(p, 0); i++) {
				papeis.add(p);
			}
		}
		List<List<DistribuicaoPessoa>> resultado = new ArrayList<>();
		for (int f = 0; f < familias.size(); f++) {
			List<DistribuicaoPessoa> lista = new ArrayList<>();
			for (int m = 0; m < familias.get(f).membros().size(); m++) {
				lista.add(null);
			}
			resultado.add(lista);
		}
		int j = 0;
		for (PapelFamiliar papel : PapelFamiliar.values()) {
			for (int f = 0; f < familias.size(); f++) {
				List<PessoaEntrada> membros = familias.get(f).membros();
				for (int m = 0; m < membros.size(); m++) {
					if (membros.get(m).papel() == papel) {
						Profissao prof = j < papeis.size() ? papeis.get(j) : CARREGADOR;
						j++;
						resultado.get(f).set(m, distribuirPessoa(prof));
					}
				}
			}
		}
		return resultado;
	}

	/** Profissão principal: maior valor; empate pela ordem do enum; null se tudo 0. */
	public static Profissao principal(Map<Profissao, Integer> profissoes) {
		Profissao melhor = null;
		int v = 0;
		for (Profissao p : Profissao.values()) {
			int x = profissoes.getOrDefault(p, 0);
			if (x > v) {
				v = x;
				melhor = p;
			}
		}
		return melhor;
	}

	/** Índice do líder (adulto mais velho; empate pela ordem do papel); -1 se não houver adulto. */
	public static int liderDaFamilia(FamiliaEntrada familia) {
		int melhor = -1;
		Comparator<PessoaEntrada> cmp = Comparator.comparingInt(PessoaEntrada::idadeAnos).reversed()
				.thenComparing(PessoaEntrada::papel);
		List<PessoaEntrada> membros = familia.membros();
		for (int i = 0; i < membros.size(); i++) {
			if (membros.get(i).idadeAnos() >= 18 && (melhor < 0 || cmp.compare(membros.get(i), membros.get(melhor)) < 0)) {
				melhor = i;
			}
		}
		return melhor;
	}

	public static int bonusLider(int car) {
		return Math.min(10, car / 2);
	}

	/** Índice da família cujo líder tem maior CAR; empate → a primeira. */
	public static int familiaLiderSugerida(List<FamiliaEntrada> familias, List<List<DistribuicaoPessoa>> pontos) {
		int idx = 0;
		int carIdx = carDoLider(familias.get(0), pontos.get(0));
		for (int i = 1; i < familias.size(); i++) {
			int car = carDoLider(familias.get(i), pontos.get(i));
			if (car > carIdx) {
				idx = i;
				carIdx = car;
			}
		}
		return idx;
	}

	private static int carDoLider(FamiliaEntrada familia, List<DistribuicaoPessoa> pontos) {
		int lider = liderDaFamilia(familia);
		return lider < 0 ? 0 : pontos.get(lider).caracteristicas().get(CAR);
	}

	/** Valida limites 20/10 e mínimos de principais; devolve códigos de {@link CodigoErro} (vazia = ok). */
	public static List<String> validar(List<DistribuicaoPessoa> pessoas) {
		List<String> erros = new ArrayList<>();
		boolean limCar = false;
		boolean limProf = false;
		EnumMap<Profissao, Integer> contagem = new EnumMap<>(Profissao.class);
		for (DistribuicaoPessoa p : pessoas) {
			if (p.caracteristicas().values().stream().anyMatch(v -> v < 0)
					|| soma(p.caracteristicas().values()) > LIMITE_CARACTERISTICAS) {
				limCar = true;
			}
			if (p.profissoes().values().stream().anyMatch(v -> v < 0)
					|| soma(p.profissoes().values()) > LIMITE_PROFISSOES) {
				limProf = true;
			}
			Profissao pr = principal(p.profissoes());
			if (pr != null) {
				contagem.merge(pr, 1, Integer::sum);
			}
		}
		if (limCar) {
			erros.add(CodigoErro.LIMITE_CARACTERISTICAS);
		}
		if (limProf) {
			erros.add(CodigoErro.LIMITE_PROFISSOES);
		}
		if (contagem.getOrDefault(CONSTRUTOR, 0) < MINIMOS.get(CONSTRUTOR)) {
			erros.add(CodigoErro.MINIMO_CONSTRUTORES);
		}
		if (contagem.getOrDefault(CARREGADOR, 0) < MINIMOS.get(CARREGADOR)) {
			erros.add(CodigoErro.MINIMO_CARREGADORES);
		}
		return erros;
	}

	private static int soma(Iterable<Integer> valores) {
		int s = 0;
		for (int v : valores) {
			s += v;
		}
		return s;
	}

}
