package com.example.loginbase.jogo.cidadao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.modelo.Vila;

/**
 * Geração da população inicial: 4 famílias de 4 membros (pai e mãe com 40 anos, filho e filha com 18),
 * determinística a partir de uma semente.
 */
@Service
public class FamiliaService {

	public static final int FAMILIAS_INICIAIS = 4;
	public static final int IDADE_PAIS_MESES = 40 * 12;
	public static final int IDADE_FILHOS_MESES = 18 * 12;
	public static final int PONTOS_CARACTERISTICAS_INICIAIS = 20;
	public static final int PONTOS_PROFISSOES_INICIAIS = 10;

	private final FamiliaRepository familiaRepository;
	private final CidadaoRepository cidadaoRepository;

	public FamiliaService(FamiliaRepository familiaRepository, CidadaoRepository cidadaoRepository) {
		this.familiaRepository = familiaRepository;
		this.cidadaoRepository = cidadaoRepository;
	}

	/**
	 * Gera as 4 famílias, cada uma na casa correspondente de {@code casaIds} (mesma ordem), e persiste tudo.
	 *
	 * @param casaIds ids das 4 casas iniciais
	 * @param semente semente da geração (mesma semente, mesmos nomes)
	 */
	@Transactional
	public List<Familia> gerarFamiliasIniciais(Vila vila, List<Long> casaIds, long semente) {
		if (casaIds == null || casaIds.size() != FAMILIAS_INICIAIS) {
			throw new IllegalArgumentException("São necessárias " + FAMILIAS_INICIAIS + " casas iniciais");
		}
		Random random = new Random(semente ^ 0x9E3779B97F4A7C15L);
		List<String> sobrenomes = embaralhada(NomesFixos.SOBRENOMES, random);
		List<String> masculinos = embaralhada(NomesFixos.MASCULINOS, random);
		List<String> femininos = embaralhada(NomesFixos.FEMININOS, random);

		List<Familia> familias = new ArrayList<>(FAMILIAS_INICIAIS);
		for (int i = 0; i < FAMILIAS_INICIAIS; i++) {
			Familia familia = familiaRepository.save(new Familia(vila.getId(), sobrenomes.get(i), casaIds.get(i)));
			Cidadao pai = gerarCidadao(familia, masculinos.get(i * 2), Sexo.M, IDADE_PAIS_MESES);
			Cidadao mae = gerarCidadao(familia, femininos.get(i * 2), Sexo.F, IDADE_PAIS_MESES);
			Cidadao filho = gerarCidadao(familia, masculinos.get(i * 2 + 1), Sexo.M, IDADE_FILHOS_MESES);
			Cidadao filha = gerarCidadao(familia, femininos.get(i * 2 + 1), Sexo.F, IDADE_FILHOS_MESES);
			cidadaoRepository.saveAll(List.of(pai, mae));
			pai.setConjugeId(mae.getId());
			mae.setConjugeId(pai.getId());
			filho.setPaiId(pai.getId());
			filho.setMaeId(mae.getId());
			filha.setPaiId(pai.getId());
			filha.setMaeId(mae.getId());
			cidadaoRepository.saveAll(List.of(pai, mae, filho, filha));
			familias.add(familia);
		}
		return familias;
	}

	/** Cidadão sem persistir: características 0 e pontos pendentes iniciais (20 de características, 10 de profissões). */
	Cidadao gerarCidadao(Familia familia, String nome, Sexo sexo, int idadeMeses) {
		Cidadao c = new Cidadao(familia.getVilaId(), familia.getId(), nome, sexo, idadeMeses);
		c.setPontosCarPendentes(PONTOS_CARACTERISTICAS_INICIAIS);
		c.setPontosProfPendentes(PONTOS_PROFISSOES_INICIAIS);
		return c;
	}

	private static List<String> embaralhada(List<String> lista, Random random) {
		List<String> copia = new ArrayList<>(lista);
		Collections.shuffle(copia, random);
		return copia;
	}

}
