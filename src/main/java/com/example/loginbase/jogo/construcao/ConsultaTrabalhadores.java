package com.example.loginbase.jogo.construcao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EficienciaService;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

/**
 * Consulta única dos trabalhadores ativos (vivos e alocados) de um prédio, com a eficiência de cada um.
 * Usada por obras, produção, ouro passivo e mercado.
 *
 * <p>Assinaturas: {@code List<Trabalhador> trabalhadores(Construcao, Vila)},
 * {@code trabalhadores(Construcao)} e {@code trabalhadores(Long construcaoId)}.
 * A eficiência usa o multiplicador do nível do prédio quando ATIVA (1,0/1,2/1,5); em obra/upgrade, 1,0
 * (o fator 0,5 do Carregador na obra é responsabilidade de quem consome).
 */
@Service
public class ConsultaTrabalhadores {

	/** Trabalhador alocado: cidadão, profissão exercida no prédio e eficiência já calculada. */
	public record Trabalhador(Cidadao cidadao, Profissao profissao, double eficiencia) {
	}

	private final CidadaoRepository cidadaoRepository;
	private final EficienciaService eficienciaService;
	private final ConstrucaoRepository construcaoRepository;
	private final VilaRepository vilaRepository;

	public ConsultaTrabalhadores(CidadaoRepository cidadaoRepository, EficienciaService eficienciaService,
			ConstrucaoRepository construcaoRepository, VilaRepository vilaRepository) {
		this.cidadaoRepository = cidadaoRepository;
		this.eficienciaService = eficienciaService;
		this.construcaoRepository = construcaoRepository;
		this.vilaRepository = vilaRepository;
	}

	@Transactional(readOnly = true)
	public List<Trabalhador> trabalhadores(Long construcaoId) {
		return construcaoRepository.findById(construcaoId).map(this::trabalhadores).orElse(List.of());
	}

	@Transactional(readOnly = true)
	public List<Trabalhador> trabalhadores(Construcao construcao) {
		return trabalhadores(construcao, vilaRepository.findById(construcao.getVilaId()).orElse(null));
	}

	@Transactional(readOnly = true)
	public List<Trabalhador> trabalhadores(Construcao construcao, Vila vila) {
		double mult = construcao.getEstado() == EstadoConstrucao.ATIVA
				? ConstrucaoCatalogo.multiplicador(construcao.getNivel()) : 1.0;
		return cidadaoRepository.findByConstrucaoIdAndVivoTrue(construcao.getId()).stream()
				.sorted(java.util.Comparator.comparing(Cidadao::getId))
				.map(c -> {
					Profissao p = profissaoDe(construcao, c);
					return new Trabalhador(c, p, eficienciaService.eficiencia(c, p, mult, vila));
				}).toList();
	}

	private static Profissao profissaoDe(Construcao construcao, Cidadao c) {
		if (c.getProfissaoTrabalho() != null) {
			return c.getProfissaoTrabalho();
		}
		List<Profissao> catalogo = ConstrucaoCatalogo.profissoes(construcao.getTipo());
		return catalogo.isEmpty() ? Profissao.CONSTRUTOR : catalogo.get(0);
	}

}
