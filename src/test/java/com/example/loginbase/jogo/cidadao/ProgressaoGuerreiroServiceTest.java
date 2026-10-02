package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ProgressaoGuerreiroServiceTest {

	private final CidadaoProfissaoRepository repo = mock(CidadaoProfissaoRepository.class);
	private final ProgressaoGuerreiroService servico = new ProgressaoGuerreiroService(repo);

	private Cidadao cidadao(String xp) {
		Cidadao c = new Cidadao(1L, 1L, "G", Sexo.M, 240);
		c.setId(7L);
		c.setXpGuerreiro(new BigDecimal(xp));
		return c;
	}

	@Test
	void somaSemConverterAbaixoDe10() {
		Cidadao c = cidadao("0");
		assertThat(servico.adicionarXp(c, new BigDecimal("0.5"))).isZero();
		assertThat(c.getXpGuerreiro()).isEqualByComparingTo("0.5");
		verify(repo, never()).save(any());
	}

	@Test
	void converte10XpEmUmPeEZeraXp() {
		CidadaoProfissao p = new CidadaoProfissao(7L, Profissao.GUERREIRO, 8);
		when(repo.findByCidadaoIdAndProfissao(7L, Profissao.GUERREIRO)).thenReturn(Optional.of(p));
		Cidadao c = cidadao("9.5");
		assertThat(servico.adicionarXp(c, new BigDecimal("0.5"))).isEqualTo(1);
		assertThat(c.getXpGuerreiro()).isEqualByComparingTo("0");
		assertThat(p.getPontosBase()).isEqualTo(9);
	}

	@Test
	void preservaOResto() {
		CidadaoProfissao p = new CidadaoProfissao(7L, Profissao.GUERREIRO, 1);
		when(repo.findByCidadaoIdAndProfissao(7L, Profissao.GUERREIRO)).thenReturn(Optional.of(p));
		Cidadao c = cidadao("9.5");
		assertThat(servico.adicionarXp(c, new BigDecimal("1.5"))).isEqualTo(1);
		assertThat(c.getXpGuerreiro()).isEqualByComparingTo("1.0");
		assertThat(p.getPontosBase()).isEqualTo(2);
	}

	@Test
	void criaLinhaDeProfissaoSeFaltar() {
		when(repo.findByCidadaoIdAndProfissao(7L, Profissao.GUERREIRO)).thenReturn(Optional.empty());
		Cidadao c = cidadao("9.5");
		assertThat(servico.adicionarXp(c, new BigDecimal("0.5"))).isEqualTo(1);
		verify(repo).save(org.mockito.ArgumentMatchers.argThat(p -> p.getPontosBase() == 1
				&& p.getProfissao() == Profissao.GUERREIRO && p.getCidadaoId().equals(7L)));
	}

	@Test
	void xpNuloOuZeroNaoFazNada() {
		Cidadao c = cidadao("3");
		assertThat(servico.adicionarXp(c, BigDecimal.ZERO)).isZero();
		assertThat(servico.adicionarXp(c, null)).isZero();
		assertThat(c.getXpGuerreiro()).isEqualByComparingTo("3");
	}

}
