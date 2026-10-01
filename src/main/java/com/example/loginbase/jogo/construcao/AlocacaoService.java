package com.example.loginbase.jogo.construcao;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.modelo.Vila;

/** Alocação de cidadãos em prédios (fonte única: cidadao.construcao_id + profissao_trabalho). */
@Service
public class AlocacaoService {

	static final int IDADE_MIN = 14;
	static final int IDADE_MAX = 64;

	private final ConstrucaoRepository construcaoRepository;
	private final CidadaoRepository cidadaoRepository;
	private final ConsultaTrabalhadores consulta;

	public AlocacaoService(ConstrucaoRepository construcaoRepository, CidadaoRepository cidadaoRepository,
			ConsultaTrabalhadores consulta) {
		this.construcaoRepository = construcaoRepository;
		this.cidadaoRepository = cidadaoRepository;
		this.consulta = consulta;
	}

	@Transactional
	public AlocacaoDTO alocar(Vila vila, Long construcaoId, Long cidadaoId, Profissao profissao) {
		Construcao c = construcao(vila, construcaoId);
		if (cidadaoId == null) {
			throw erro("Informe o cidadão");
		}
		Cidadao cid = cidadaoRepository.findById(cidadaoId)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Cidadão não encontrado"));
		if (!cid.getVilaId().equals(vila.getId())) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Cidadão pertence a outra vila");
		}
		if (!cid.isVivo()) {
			throw erro("Cidadão morto não pode ser alocado");
		}
		if (cid.getIdadeAnos() < IDADE_MIN || cid.getIdadeAnos() > IDADE_MAX) {
			throw erro("Idade fora do intervalo de trabalho (14–64)");
		}
		if (cid.getTropaId() != null) {
			throw erro("Membro de tropa não pode ser alocado");
		}
		if (cid.getConstrucaoId() != null) {
			throw erro(cid.getConstrucaoId().equals(c.getId()) ? "Cidadão já está alocado neste prédio"
					: "Cidadão já está alocado em outro prédio");
		}
		Profissao escolhida;
		int limite;
		if (c.getEstado() == EstadoConstrucao.ATIVA) {
			List<Profissao> permitidas = ConstrucaoCatalogo.profissoes(c.getTipo());
			if (permitidas.isEmpty()) {
				throw erro("Este prédio não aceita trabalhadores");
			}
			if (profissao == null) {
				if (permitidas.size() > 1) {
					throw erro("Informe a profissão (" + permitidas + ")");
				}
				escolhida = permitidas.get(0);
			} else if (!permitidas.contains(profissao)) {
				throw erro("Profissão inválida para este prédio");
			} else {
				escolhida = profissao;
			}
			limite = ConstrucaoCatalogo.vagas(c.getNivel());
		} else {
			if (profissao != Profissao.CONSTRUTOR && profissao != Profissao.CARREGADOR) {
				throw erro("Em obra só é possível alocar Construtor ou Carregador");
			}
			escolhida = profissao;
			limite = ConstrucaoCatalogo.maxTrabalhadoresObra(c.getNivel());
		}
		if (cidadaoRepository.findByConstrucaoIdAndVivoTrue(c.getId()).size() >= limite) {
			throw erro("Sem vagas disponíveis");
		}
		cid.setConstrucaoId(c.getId());
		cid.setProfissaoTrabalho(escolhida);
		cidadaoRepository.saveAndFlush(cid);
		return consulta.trabalhadores(c, vila).stream().filter(t -> t.cidadao().getId().equals(cid.getId()))
				.findFirst().map(AlocacaoDTO::de).orElseThrow();
	}

	@Transactional
	public void desalocar(Vila vila, Long construcaoId, Long cidadaoId) {
		Construcao c = construcao(vila, construcaoId);
		Cidadao cid = cidadaoRepository.findById(cidadaoId)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Cidadão não encontrado"));
		if (!cid.getVilaId().equals(vila.getId())) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Cidadão pertence a outra vila");
		}
		if (!c.getId().equals(cid.getConstrucaoId())) {
			throw erro("Cidadão não está alocado neste prédio");
		}
		cid.setConstrucaoId(null);
		cid.setProfissaoTrabalho(null);
		cidadaoRepository.saveAndFlush(cid);
	}

	@Transactional(readOnly = true)
	public List<AlocacaoDTO> listar(Vila vila, Long construcaoId) {
		return consulta.trabalhadores(construcao(vila, construcaoId), vila).stream().map(AlocacaoDTO::de).toList();
	}

	private Construcao construcao(Vila vila, Long id) {
		Construcao c = construcaoRepository.findById(id)
				.orElseThrow(() -> new JogoException(HttpStatus.NOT_FOUND, "Construção não encontrada"));
		if (!c.getVilaId().equals(vila.getId())) {
			throw new JogoException(HttpStatus.FORBIDDEN, "Construção pertence a outra vila");
		}
		return c;
	}

	private static JogoException erro(String msg) {
		return new JogoException(HttpStatus.BAD_REQUEST, msg);
	}

}
