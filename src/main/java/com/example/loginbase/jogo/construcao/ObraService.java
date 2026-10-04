package com.example.loginbase.jogo.construcao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.construcao.ConsultaTrabalhadores.Trabalhador;
import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.servico.BonusRegiaoService;
import com.example.loginbase.jogo.turno.RegistroEventoTurnoService;
import com.example.loginbase.jogo.turno.TipoEventoTurno;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Progresso de obras e upgrades no turno (passo 5). Cada Construtor soma eficiência x 1,0 e cada Carregador
 * eficiência x 0,5 de PO por turno (até o limite de trabalhadores do nível). A PO é inteira na coluna
 * {@code po_atual}; a fração acumulada fica em {@code configuracao.poFracao}.
 */
@Service
public class ObraService {

	static final String CHAVE_FRACAO = "poFracao";

	private final ConstrucaoRepository construcaoRepository;
	private final CidadaoRepository cidadaoRepository;
	private final ConsultaTrabalhadores consultaTrabalhadores;
	private final RegistroEventoTurnoService registro;
	private final BonusRegiaoService bonusRegiaoService;
	private final ObjectMapper mapper = new ObjectMapper();

	public ObraService(ConstrucaoRepository construcaoRepository, CidadaoRepository cidadaoRepository,
			ConsultaTrabalhadores consultaTrabalhadores, RegistroEventoTurnoService registro,
			BonusRegiaoService bonusRegiaoService) {
		this.construcaoRepository = construcaoRepository;
		this.cidadaoRepository = cidadaoRepository;
		this.consultaTrabalhadores = consultaTrabalhadores;
		this.registro = registro;
		this.bonusRegiaoService = bonusRegiaoService;
	}

	@Transactional
	public void processarObras(Vila vila, int turno) {
		List<Construcao> obras = new ArrayList<>(construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.EM_OBRA));
		obras.addAll(construcaoRepository.findByVilaIdAndEstado(vila.getId(), EstadoConstrucao.EM_UPGRADE));
		obras.sort(java.util.Comparator.comparing(Construcao::getId));
		for (Construcao obra : obras) {
			processar(vila, turno, obra);
		}
	}

	private void processar(Vila vila, int turno, Construcao obra) {
		int limite = ConstrucaoCatalogo.maxTrabalhadoresObra(obra.getNivel());
		List<Trabalhador> trabalhadores = consultaTrabalhadores.trabalhadores(obra, vila).stream()
				.filter(t -> t.profissao() == Profissao.CONSTRUTOR || t.profissao() == Profissao.CARREGADOR)
				.limit(limite).toList();
		double ganho = 0;
		for (Trabalhador t : trabalhadores) {
			ganho += t.eficiencia() * (t.profissao() == Profissao.CONSTRUTOR ? 1.0 : 0.5);
		}
		ganho *= 1 + bonusRegiaoService.bonus(vila.getId(), BonusRegiao.DESENVOLVIMENTO) / 100.0;
		double acumulado = lerFracao(obra) + ganho;
		int inteiro = (int) Math.floor(acumulado + 1e-9);
		double fracao = Math.max(0, acumulado - inteiro);
		int novoPo = Math.min(obra.getPoTotal(), obra.getPoAtual() + inteiro);
		obra.setPoAtual(novoPo);
		boolean concluida = novoPo >= obra.getPoTotal();
		boolean upgrade = obra.getEstado() == EstadoConstrucao.EM_UPGRADE;
		gravarFracao(obra, concluida ? 0 : fracao);
		if (concluida) {
			obra.setEstado(EstadoConstrucao.ATIVA);
			desalocarTrabalhadoresDeObra(obra);
			TipoEventoTurno tipo = upgrade ? TipoEventoTurno.UPGRADE_CONCLUIDO : TipoEventoTurno.OBRA_CONCLUIDA;
			String nome = ConstrucaoCatalogo.de(obra.getTipo()).nome();
			registro.registrar(vila, turno, tipo,
					(upgrade ? "Upgrade concluído: " : "Obra concluída: ") + nome,
					Map.of("construcaoId", obra.getId(), "tipo", obra.getTipo().name(),
							"nivel", obra.getNivel().name()));
		}
		construcaoRepository.save(obra);
	}

	private void desalocarTrabalhadoresDeObra(Construcao obra) {
		List<Profissao> catalogo = ConstrucaoCatalogo.profissoes(obra.getTipo());
		for (Cidadao c : cidadaoRepository.findByConstrucaoIdAndVivoTrue(obra.getId())) {
			Profissao p = c.getProfissaoTrabalho();
			boolean deObra = p == Profissao.CONSTRUTOR || p == Profissao.CARREGADOR;
			if (deObra && !catalogo.contains(p)) {
				c.setConstrucaoId(null);
				c.setProfissaoTrabalho(null);
				cidadaoRepository.save(c);
			}
		}
	}

	private double lerFracao(Construcao obra) {
		if (obra.getConfiguracao() == null || obra.getConfiguracao().isBlank()) {
			return 0;
		}
		try {
			JsonNode no = mapper.readTree(obra.getConfiguracao());
			return no.path(CHAVE_FRACAO).asDouble(0);
		} catch (RuntimeException e) {
			return 0;
		}
	}

	private void gravarFracao(Construcao obra, double fracao) {
		try {
			JsonNode atual = obra.getConfiguracao() == null || obra.getConfiguracao().isBlank() ? null
					: mapper.readTree(obra.getConfiguracao());
			ObjectNode no = atual != null && atual.isObject() ? (ObjectNode) atual : mapper.createObjectNode();
			if (fracao > 0) {
				no.put(CHAVE_FRACAO, fracao);
			} else {
				no.remove(CHAVE_FRACAO);
			}
			obra.setConfiguracao(no.isEmpty() ? null : mapper.writeValueAsString(no));
		} catch (RuntimeException e) {
			// configuração ilegível: não persiste fração
		}
	}

}
