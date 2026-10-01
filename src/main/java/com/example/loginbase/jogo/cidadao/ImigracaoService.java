package com.example.loginbase.jogo.cidadao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Passo 9 do turno (parte de imigração): cada Estalagem ATIVA sorteia, com chance de 2% x nível, a chegada de um
 * adulto (18-30 anos) que forma um novo núcleo solteiro numa casa ativa com núcleo e vaga livres.
 */
@Service
public class ImigracaoService {

	public static final double CHANCE_POR_NIVEL = 0.02;
	public static final int IDADE_MIN_ANOS = 18;
	public static final int IDADE_MAX_ANOS = 30;
	public static final int PONTOS_CARACTERISTICAS = 20;
	public static final int MAX_POR_CARACTERISTICA = 10;
	public static final int PONTOS_PROFISSOES = 10;
	public static final int MAX_POR_PROFISSAO = 5;

	private final ConstrucaoRepository construcaoRepository;
	private final FamiliaRepository familiaRepository;
	private final CidadaoRepository cidadaoRepository;
	private final CidadaoProfissaoRepository profissaoRepository;
	private final OcupacaoCasasService ocupacaoCasasService;
	private final RegistroEventoTurnoService registro;
	private final RandomGenerator random;

	@Autowired
	public ImigracaoService(ConstrucaoRepository construcaoRepository, FamiliaRepository familiaRepository,
			CidadaoRepository cidadaoRepository, CidadaoProfissaoRepository profissaoRepository,
			OcupacaoCasasService ocupacaoCasasService, RegistroEventoTurnoService registro) {
		this(construcaoRepository, familiaRepository, cidadaoRepository, profissaoRepository, ocupacaoCasasService,
				registro, RandomGenerator.getDefault());
	}

	public ImigracaoService(ConstrucaoRepository construcaoRepository, FamiliaRepository familiaRepository,
			CidadaoRepository cidadaoRepository, CidadaoProfissaoRepository profissaoRepository,
			OcupacaoCasasService ocupacaoCasasService, RegistroEventoTurnoService registro, RandomGenerator random) {
		this.construcaoRepository = construcaoRepository;
		this.familiaRepository = familiaRepository;
		this.cidadaoRepository = cidadaoRepository;
		this.profissaoRepository = profissaoRepository;
		this.ocupacaoCasasService = ocupacaoCasasService;
		this.registro = registro;
		this.random = random;
	}

	public static double chance(NivelConstrucao nivel) {
		return CHANCE_POR_NIVEL * (nivel.ordinal() + 1);
	}

	/** Processa a imigração de todas as Estalagens ativas da vila. Retorna os imigrantes do turno. */
	@Transactional
	public List<Cidadao> processarImigracao(Vila vila, int turno) {
		List<Cidadao> imigrantes = new ArrayList<>();
		List<Construcao> estalagens = construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.ATIVA)
				.stream().filter(c -> c.getTipo() == TipoConstrucao.ESTALAGEM)
				.sorted((a, b) -> a.getId().compareTo(b.getId())).toList();
		for (Construcao estalagem : estalagens) {
			if (random.nextDouble() >= chance(estalagem.getNivel())) {
				continue;
			}
			// recalculado a cada imigrante: a ocupação muda
			OcupacaoCasasService.OcupacaoCasa casa = ocupacaoCasasService.casasAtivas(vila.getId()).stream()
					.filter(o -> o.nucleosLivres() >= 1 && o.vagasLivres() >= 1).findFirst().orElse(null);
			if (casa == null) {
				continue;
			}
			imigrantes.add(receber(vila, turno, estalagem, casa));
		}
		return imigrantes;
	}

	private Cidadao receber(Vila vila, int turno, Construcao estalagem, OcupacaoCasasService.OcupacaoCasa casa) {
		Sexo sexo = random.nextBoolean() ? Sexo.M : Sexo.F;
		List<String> nomes = sexo == Sexo.M ? NomesFixos.MASCULINOS : NomesFixos.FEMININOS;
		String sobrenome = NomesFixos.SOBRENOMES.get(random.nextInt(NomesFixos.SOBRENOMES.size()));
		String nome = nomes.get(random.nextInt(nomes.size()));
		int idadeMeses = IDADE_MIN_ANOS * 12 + random.nextInt((IDADE_MAX_ANOS - IDADE_MIN_ANOS + 1) * 12);

		Familia familia = familiaRepository.save(new Familia(vila.getId(), sobrenome, casa.casaId()));
		Cidadao c = new Cidadao(vila.getId(), familia.getId(), nome + " " + sobrenome, sexo, idadeMeses);
		int[] car = distribuir(Caracteristica.values().length, PONTOS_CARACTERISTICAS, MAX_POR_CARACTERISTICA);
		Caracteristica[] cs = Caracteristica.values();
		for (int i = 0; i < cs.length; i++) {
			switch (cs[i]) {
				case VIT -> c.setVit(car[i]);
				case FOR -> c.setForca(car[i]);
				case VEL -> c.setVel(car[i]);
				case INT -> c.setInteligencia(car[i]);
				case CAR -> c.setCar(car[i]);
			}
		}
		c = cidadaoRepository.save(c);

		Profissao[] ps = Profissao.values();
		int[] pe = distribuir(ps.length, PONTOS_PROFISSOES, MAX_POR_PROFISSAO);
		for (int i = 0; i < ps.length; i++) {
			if (pe[i] > 0) {
				profissaoRepository.save(new CidadaoProfissao(c.getId(), ps[i], pe[i]));
			}
		}

		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("cidadaoId", c.getId());
		dados.put("nome", c.getNome());
		dados.put("sexo", sexo.name());
		dados.put("idadeAnos", c.getIdadeAnos());
		dados.put("familiaId", familia.getId());
		dados.put("casaId", casa.casaId());
		dados.put("estalagemId", estalagem.getId());
		registro.registrar(vila, turno, TipoEventoTurno.IMIGRACAO, c.getNome() + " chegou à vila pela Estalagem.",
				dados);
		return c;
	}

	/** Distribui {@code total} pontos, um a um, em posições sorteadas que ainda não atingiram {@code maximo}. */
	private int[] distribuir(int posicoes, int total, int maximo) {
		int[] v = new int[posicoes];
		for (int p = 0; p < total; p++) {
			List<Integer> abertas = new ArrayList<>();
			for (int i = 0; i < posicoes; i++) {
				if (v[i] < maximo) {
					abertas.add(i);
				}
			}
			if (abertas.isEmpty()) {
				break;
			}
			v[abertas.get(random.nextInt(abertas.size()))]++;
		}
		return v;
	}

}
