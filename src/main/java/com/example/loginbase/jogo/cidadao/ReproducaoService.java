package com.example.loginbase.jogo.cidadao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Passo 9 do turno (parte de reprodução): gestações em curso, nascimentos e novas concepções.
 *
 * <p>A concepção ocorre no turno T (gestação = 9) e o nascimento no turno T+9. A vaga do bebê já está reservada
 * na casa pela gestação (ver {@link OcupacaoCasasService}), então só é necessário haver 1 vaga livre para conceber.
 */
@Service
public class ReproducaoService {

	public static final int IDADE_MIN_MAE = 18;
	public static final int IDADE_MAX_MAE = 45;
	public static final int TURNOS_GESTACAO = 9;
	public static final int INTERVALO_PARTOS = 12;
	public static final double CHANCE_CONCEPCAO = 0.08;

	private final CidadaoRepository cidadaoRepository;
	private final CidadaoProfissaoRepository profissaoRepository;
	private final FamiliaRepository familiaRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final OcupacaoCasasService ocupacaoCasasService;
	private final RegistroEventoTurnoService registro;
	private final RandomGenerator random;

	@Autowired
	public ReproducaoService(CidadaoRepository cidadaoRepository, CidadaoProfissaoRepository profissaoRepository,
			FamiliaRepository familiaRepository, ConstrucaoRepository construcaoRepository,
			OcupacaoCasasService ocupacaoCasasService, RegistroEventoTurnoService registro) {
		this(cidadaoRepository, profissaoRepository, familiaRepository, construcaoRepository, ocupacaoCasasService,
				registro, RandomGenerator.getDefault());
	}

	public ReproducaoService(CidadaoRepository cidadaoRepository, CidadaoProfissaoRepository profissaoRepository,
			FamiliaRepository familiaRepository, ConstrucaoRepository construcaoRepository,
			OcupacaoCasasService ocupacaoCasasService, RegistroEventoTurnoService registro, RandomGenerator random) {
		this.cidadaoRepository = cidadaoRepository;
		this.profissaoRepository = profissaoRepository;
		this.familiaRepository = familiaRepository;
		this.construcaoRepository = construcaoRepository;
		this.ocupacaoCasasService = ocupacaoCasasService;
		this.registro = registro;
		this.random = random;
	}

	/** Processa gestações (nascimentos) e depois concepções. Retorna os cidadãos nascidos no turno. */
	@Transactional
	public List<Cidadao> processar(Vila vila, int turno) {
		List<Cidadao> nascidos = new ArrayList<>();
		List<Cidadao> vivos = cidadaoRepository.findByVilaIdAndVivoTrue(vila.getId());
		Map<Long, Cidadao> porId = new HashMap<>();
		vivos.forEach(c -> porId.put(c.getId(), c));

		// 1) gestações em curso (concebidas em turnos anteriores)
		List<Cidadao> gestantes = vivos.stream()
				.filter(c -> c.getGestacaoTurnos() != null && c.getGestacaoTurnos() > 0)
				.sorted((a, b) -> a.getId().compareTo(b.getId())).toList();
		for (Cidadao mae : gestantes) {
			int restante = mae.getGestacaoTurnos() - 1;
			if (restante > 0) {
				mae.setGestacaoTurnos(restante);
				cidadaoRepository.save(mae);
			} else {
				nascidos.add(nascer(vila, mae, porId.get(mae.getConjugeId()), turno));
			}
		}

		// 2) novas concepções
		for (Cidadao pai : cidadaoRepository.findCasaisVivos(vila.getId())) {
			Cidadao mae = pai.getConjugeId() == null ? null : porId.get(pai.getConjugeId());
			if (mae == null || !podeConceberAgora(pai, mae, turno)) {
				continue;
			}
			Optional<Construcao> casa = casaDe(mae);
			if (casa.isEmpty() || ocupacaoCasasService.ocupacao(casa.get()).vagasLivres() < 1) {
				continue;
			}
			if (random.nextDouble() < CHANCE_CONCEPCAO) {
				mae.setGestacaoTurnos(TURNOS_GESTACAO);
				cidadaoRepository.save(mae);
				Map<String, Object> dados = new LinkedHashMap<>();
				dados.put("maeId", mae.getId());
				dados.put("paiId", pai.getId());
				dados.put("casaId", casa.get().getId());
				registro.registrar(vila, turno, TipoEventoTurno.CONCEPCAO,
						mae.getNome() + " está esperando um bebê.", dados);
			}
		}
		return nascidos;
	}

	/** Regras de elegibilidade, exceto vaga na casa e sorteio. */
	public boolean podeConceberAgora(Cidadao pai, Cidadao mae, int turno) {
		if (!mae.isVivo() || !pai.isVivo() || mae.getSexo() != Sexo.F) {
			return false;
		}
		int anos = mae.getIdadeAnos();
		if (anos < IDADE_MIN_MAE || anos > IDADE_MAX_MAE) {
			return false;
		}
		if (mae.getFamintoTurnos() > 0 || pai.getFamintoTurnos() > 0) {
			return false;
		}
		if (mae.getGestacaoTurnos() != null) {
			return false;
		}
		return mae.getTurnoUltimoParto() == null || turno - mae.getTurnoUltimoParto() >= INTERVALO_PARTOS;
	}

	/** floor(0,25 x (pai + mae) / 2) */
	public static int calcularCaracteristicaHerdada(int pai, int mae) {
		return (pai + mae) / 8;
	}

	private Optional<Construcao> casaDe(Cidadao mae) {
		return familiaRepository.findById(mae.getFamiliaId()).map(Familia::getCasaId)
				.flatMap(construcaoRepository::findById)
				.filter(c -> c.getEstado() == EstadoConstrucao.ATIVA);
	}

	private Cidadao nascer(Vila vila, Cidadao mae, Cidadao pai, int turno) {
		Familia familia = familiaRepository.findById(mae.getFamiliaId()).orElseThrow();
		Sexo sexo = random.nextBoolean() ? Sexo.M : Sexo.F;
		List<String> nomes = sexo == Sexo.M ? NomesFixos.MASCULINOS : NomesFixos.FEMININOS;
		String nome = nomes.get(random.nextInt(nomes.size())) + " " + familia.getSobrenome();

		Cidadao bebe = new Cidadao(vila.getId(), mae.getFamiliaId(), nome, sexo, 0);
		bebe.setMaeId(mae.getId());
		bebe.setPaiId(pai == null ? null : pai.getId());
		Cidadao ref = pai != null ? pai : mae;
		bebe.setVit(calcularCaracteristicaHerdada(ref.getVit(), mae.getVit()));
		bebe.setForca(calcularCaracteristicaHerdada(ref.getForca(), mae.getForca()));
		bebe.setVel(calcularCaracteristicaHerdada(ref.getVel(), mae.getVel()));
		bebe.setInteligencia(calcularCaracteristicaHerdada(ref.getInteligencia(), mae.getInteligencia()));
		bebe.setCar(calcularCaracteristicaHerdada(ref.getCar(), mae.getCar()));
		bebe = cidadaoRepository.save(bebe);

		Map<Profissao, Integer> peMae = pontos(mae.getId());
		Map<Profissao, Integer> pePai = pai == null ? peMae : pontos(pai.getId());
		for (Profissao p : Profissao.values()) {
			int herdado = calcularCaracteristicaHerdada(pePai.getOrDefault(p, 0), peMae.getOrDefault(p, 0));
			if (herdado > 0) {
				profissaoRepository.save(new CidadaoProfissao(bebe.getId(), p, herdado));
			}
		}

		mae.setGestacaoTurnos(null);
		mae.setTurnoUltimoParto(turno);
		cidadaoRepository.save(mae);

		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("cidadaoId", bebe.getId());
		dados.put("nome", bebe.getNome());
		dados.put("sexo", sexo.name());
		dados.put("maeId", mae.getId());
		dados.put("paiId", bebe.getPaiId());
		dados.put("familiaId", bebe.getFamiliaId());
		registro.registrar(vila, turno, TipoEventoTurno.NASCIMENTO, bebe.getNome() + " nasceu.", dados);
		return bebe;
	}

	private Map<Profissao, Integer> pontos(Long cidadaoId) {
		Map<Profissao, Integer> m = new HashMap<>();
		for (CidadaoProfissao cp : profissaoRepository.findByCidadaoId(cidadaoId)) {
			m.put(cp.getProfissao(), cp.getPontosBase());
		}
		return m;
	}

}
