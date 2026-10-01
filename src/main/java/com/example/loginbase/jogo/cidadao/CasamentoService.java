package com.example.loginbase.jogo.cidadao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.JogoTurnoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

/**
 * Casamento: forma um novo núcleo familiar em uma casa com núcleo e vagas livres. Os pais continuam na casa de
 * origem; o núcleo de origem só é apagado se ficar sem nenhum cidadão (e não for a família líder).
 */
@Service
public class CasamentoService {

	public static final int IDADE_MINIMA_ANOS = 18;

	private final CidadaoRepository cidadaoRepository;
	private final FamiliaRepository familiaRepository;
	private final ConstrucaoRepository construcaoRepository;
	private final VilaRepository vilaRepository;
	private final JogoTurnoRepository turnoRepository;
	private final OcupacaoCasasService ocupacaoCasas;
	private final RegistroEventoTurnoService registro;

	public CasamentoService(CidadaoRepository cidadaoRepository, FamiliaRepository familiaRepository,
			ConstrucaoRepository construcaoRepository, VilaRepository vilaRepository,
			JogoTurnoRepository turnoRepository, OcupacaoCasasService ocupacaoCasas,
			RegistroEventoTurnoService registro) {
		this.cidadaoRepository = cidadaoRepository;
		this.familiaRepository = familiaRepository;
		this.construcaoRepository = construcaoRepository;
		this.vilaRepository = vilaRepository;
		this.turnoRepository = turnoRepository;
		this.ocupacaoCasas = ocupacaoCasas;
		this.registro = registro;
	}

	@Transactional
	public Familia casar(Long vilaId, Long cidadao1Id, Long cidadao2Id, Long casaId, String sobrenomeEscolhido) {
		if (cidadao1Id == null || cidadao2Id == null || casaId == null) {
			throw erro("Informe os dois cidadãos e a casa");
		}
		if (cidadao1Id.equals(cidadao2Id)) {
			throw erro("Um cidadão não pode casar consigo mesmo");
		}
		Vila vila = vilaRepository.findById(vilaId)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Vila não encontrada"));
		Cidadao c1 = buscar(cidadao1Id, vilaId);
		Cidadao c2 = buscar(cidadao2Id, vilaId);
		Construcao casa = construcaoRepository.findById(casaId)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Casa não encontrada"));
		if (!casa.getVilaId().equals(vilaId)) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Casa pertence a outra vila");
		}
		for (Cidadao c : List.of(c1, c2)) {
			if (!c.isVivo()) {
				throw erro(c.getNome() + " está morto");
			}
			if (c.getIdadeAnos() < IDADE_MINIMA_ANOS) {
				throw erro(c.getNome() + " tem menos de " + IDADE_MINIMA_ANOS + " anos");
			}
			if (c.getConjugeId() != null) {
				throw erro(c.getNome() + " não é solteiro(a)");
			}
		}
		if (temParente1Grau(c1, c2)) {
			throw erro("Os cidadãos são parentes de 1º grau (pais ou irmãos)");
		}
		if (casa.getTipo() != TipoConstrucao.CASA) {
			throw erro("A construção escolhida não é uma casa");
		}
		if (casa.getEstado() != EstadoConstrucao.ATIVA) {
			throw erro("A casa escolhida ainda não está ativa");
		}
		String sobrenome = validarSobrenome(sobrenomeEscolhido, c1, c2);
		OcupacaoCasasService.OcupacaoCasa ocupacao = ocupacaoCasas.ocupacao(casa, Set.of(c1.getId(), c2.getId()));
		if (ocupacao.nucleosLivres() < 1) {
			throw erro("A casa escolhida não tem núcleo familiar livre");
		}
		if (ocupacao.vagasLivres() < 2) {
			throw erro("A casa escolhida não tem vagas livres para o casal");
		}

		Long origem1 = c1.getFamiliaId();
		Long origem2 = c2.getFamiliaId();
		Familia nova = familiaRepository.save(new Familia(vilaId, sobrenome, casa.getId()));
		c1.setFamiliaId(nova.getId());
		c2.setFamiliaId(nova.getId());
		c1.setConjugeId(c2.getId());
		c2.setConjugeId(c1.getId());
		cidadaoRepository.saveAll(List.of(c1, c2));
		cidadaoRepository.flush();
		apagarSeVazia(vila, origem1, nova.getId());
		if (!origem2.equals(origem1)) {
			apagarSeVazia(vila, origem2, nova.getId());
		}

		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("familiaId", nova.getId());
		dados.put("casaId", casa.getId());
		dados.put("sobrenome", sobrenome);
		dados.put("cidadao1Id", c1.getId());
		dados.put("cidadao2Id", c2.getId());
		registro.registrar(vila, turnoRepository.maiorNumero(), TipoEventoTurno.CASAMENTO,
				c1.getNome() + " e " + c2.getNome() + " se casaram e formaram a família " + sobrenome + ".", dados);
		return nova;
	}

	/** Pai/mãe de um do outro, ou irmãos (mesmo pai ou mesma mãe conhecidos). */
	public boolean temParente1Grau(Cidadao a, Cidadao b) {
		if (a.getId() != null && (a.getId().equals(b.getPaiId()) || a.getId().equals(b.getMaeId()))) {
			return true;
		}
		if (b.getId() != null && (b.getId().equals(a.getPaiId()) || b.getId().equals(a.getMaeId()))) {
			return true;
		}
		return (a.getPaiId() != null && a.getPaiId().equals(b.getPaiId()))
				|| (a.getMaeId() != null && a.getMaeId().equals(b.getMaeId()));
	}

	private Cidadao buscar(Long id, Long vilaId) {
		Cidadao c = cidadaoRepository.findById(id)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Cidadão não encontrado"));
		if (!c.getVilaId().equals(vilaId)) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Cidadão pertence a outra vila");
		}
		return c;
	}

	/** O sobrenome é escolhido entre os dois núcleos de origem. */
	private String validarSobrenome(String escolhido, Cidadao c1, Cidadao c2) {
		if (escolhido == null || escolhido.isBlank()) {
			throw erro("Informe o sobrenome do novo núcleo");
		}
		String s = escolhido.trim();
		String s1 = familiaRepository.findById(c1.getFamiliaId()).map(Familia::getSobrenome).orElse(null);
		String s2 = familiaRepository.findById(c2.getFamiliaId()).map(Familia::getSobrenome).orElse(null);
		if (!s.equalsIgnoreCase(s1) && !s.equalsIgnoreCase(s2)) {
			throw erro("O sobrenome deve ser um dos dois sobrenomes dos noivos");
		}
		return s.equalsIgnoreCase(s1) ? s1 : s2;
	}

	private void apagarSeVazia(Vila vila, Long familiaId, Long novaId) {
		if (familiaId == null || familiaId.equals(novaId)) {
			return;
		}
		if (familiaId.equals(vila.getFamiliaLiderId()) || !cidadaoRepository.findByFamiliaId(familiaId).isEmpty()) {
			return;
		}
		familiaRepository.deleteById(familiaId);
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}

}
