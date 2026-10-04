package com.example.loginbase.jogo.construcao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.Jazida;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.recurso.Recurso;

class ConstrucaoCatalogoTest {

	@Test
	void todosOsTiposEstaoNoCatalogo() {
		for (TipoConstrucao t : TipoConstrucao.values()) {
			assertNotNull(ConstrucaoCatalogo.de(t), t.name());
		}
		assertEquals(24, TipoConstrucao.values().length);
	}

	@Test
	void custosCasa() {
		assertEquals(Map.of(Recurso.MADEIRA, 20, Recurso.PEDRA, 10, Recurso.ARGILA, 10),
				ConstrucaoCatalogo.custo(TipoConstrucao.CASA, NivelConstrucao.N1));
		assertEquals(Map.of(Recurso.MADEIRA, 50, Recurso.PEDRA, 25, Recurso.ARGILA, 25),
				ConstrucaoCatalogo.custo(TipoConstrucao.CASA, NivelConstrucao.N2));
		assertEquals(Map.of(Recurso.MADEIRA, 100, Recurso.PEDRA, 50, Recurso.ARGILA, 50),
				ConstrucaoCatalogo.custo(TipoConstrucao.CASA, NivelConstrucao.N3));
	}

	@Test
	void custoArredondaParaCima() {
		Map<Recurso, Integer> n2 = ConstrucaoCatalogo.custo(TipoConstrucao.ACAMPAMENTO_LENHADORES, NivelConstrucao.N2);
		assertEquals(38, n2.get(Recurso.MADEIRA));
		assertEquals(13, n2.get(Recurso.PEDRA));
	}

	@Test
	void poSerraria() {
		assertEquals(6, ConstrucaoCatalogo.po(TipoConstrucao.SERRARIA, NivelConstrucao.N1));
		assertEquals(15, ConstrucaoCatalogo.po(TipoConstrucao.SERRARIA, NivelConstrucao.N2));
		assertEquals(30, ConstrucaoCatalogo.po(TipoConstrucao.SERRARIA, NivelConstrucao.N3));
		assertEquals(40, ConstrucaoCatalogo.po(TipoConstrucao.QUARTEL, NivelConstrucao.N3));
	}

	@Test
	void quartelPorNivel() {
		assertEquals(1, ConstrucaoCatalogo.vagasInstrutorQuartel(NivelConstrucao.N1));
		assertEquals(2, ConstrucaoCatalogo.vagasInstrutorQuartel(NivelConstrucao.N2));
		assertEquals(3, ConstrucaoCatalogo.vagasInstrutorQuartel(NivelConstrucao.N3));
		assertEquals(5, ConstrucaoCatalogo.capacidadeQuartel(NivelConstrucao.N1));
		assertEquals(8, ConstrucaoCatalogo.capacidadeQuartel(NivelConstrucao.N2));
		assertEquals(10, ConstrucaoCatalogo.capacidadeQuartel(NivelConstrucao.N3));
		assertEquals(1, ConstrucaoCatalogo.maxTropasQuartel(NivelConstrucao.N1));
		assertEquals(2, ConstrucaoCatalogo.maxTropasQuartel(NivelConstrucao.N2));
		assertEquals(4, ConstrucaoCatalogo.maxTropasQuartel(NivelConstrucao.N3));
	}

	@Test
	void bonificacoesPorNivel() {
		assertEquals(2, ConstrucaoCatalogo.vagas(NivelConstrucao.N1));
		assertEquals(5, ConstrucaoCatalogo.vagas(NivelConstrucao.N2));
		assertEquals(10, ConstrucaoCatalogo.vagas(NivelConstrucao.N3));
		assertEquals(1.0, ConstrucaoCatalogo.multiplicador(NivelConstrucao.N1));
		assertEquals(1.2, ConstrucaoCatalogo.multiplicador(NivelConstrucao.N2));
		assertEquals(1.5, ConstrucaoCatalogo.multiplicador(NivelConstrucao.N3));
		assertEquals(4, ConstrucaoCatalogo.maxLadrilhosMarcados(NivelConstrucao.N1));
		assertEquals(10, ConstrucaoCatalogo.maxLadrilhosMarcados(NivelConstrucao.N2));
		assertEquals(20, ConstrucaoCatalogo.maxLadrilhosMarcados(NivelConstrucao.N3));
		assertEquals(2, ConstrucaoCatalogo.maxTrabalhadoresObra(NivelConstrucao.N1));
		assertEquals(4, ConstrucaoCatalogo.maxTrabalhadoresObra(NivelConstrucao.N2));
		assertEquals(6, ConstrucaoCatalogo.maxTrabalhadoresObra(NivelConstrucao.N3));
		assertEquals(3, ConstrucaoCatalogo.tamanho(NivelConstrucao.N3));
	}

	@Test
	void casaNucleosEVagas() {
		assertEquals(1, ConstrucaoCatalogo.nucleosCasa(NivelConstrucao.N1));
		assertEquals(2, ConstrucaoCatalogo.nucleosCasa(NivelConstrucao.N2));
		assertEquals(4, ConstrucaoCatalogo.nucleosCasa(NivelConstrucao.N3));
		assertEquals(4, ConstrucaoCatalogo.vagasCasa(NivelConstrucao.N1));
		assertEquals(10, ConstrucaoCatalogo.vagasCasa(NivelConstrucao.N2));
		assertEquals(24, ConstrucaoCatalogo.vagasCasa(NivelConstrucao.N3));
	}

	@Test
	void armazemVagasEMinimoDeCarregadores() {
		assertEquals(2, ConstrucaoCatalogo.vagas(NivelConstrucao.N1));
		assertEquals(1, ConstrucaoCatalogo.minCarregadoresArmazem(NivelConstrucao.N1));
		assertEquals(2, ConstrucaoCatalogo.minCarregadoresArmazem(NivelConstrucao.N2));
		assertEquals(4, ConstrucaoCatalogo.minCarregadoresArmazem(NivelConstrucao.N3));
		assertEquals(java.util.List.of(com.example.loginbase.jogo.cidadao.Profissao.CARREGADOR), ConstrucaoCatalogo.profissoes(TipoConstrucao.ARMAZEM));
	}

	@Test
	void regioes() {
		assertEquals(java.util.List.of(TipoRegiao.URBANA), ConstrucaoCatalogo.regioesPermitidas(TipoConstrucao.QUARTEL));
		assertFalse(ConstrucaoCatalogo.permiteRegiao(TipoConstrucao.QUARTEL, TipoRegiao.PLANICIE));
		assertTrue(ConstrucaoCatalogo.permiteRegiao(TipoConstrucao.FAZENDA_PLANTIO, TipoRegiao.PLANICIE));
		assertTrue(ConstrucaoCatalogo.permiteRegiao(TipoConstrucao.PEDREIRA, TipoRegiao.MONTANHA));
		assertFalse(ConstrucaoCatalogo.permiteRegiao(TipoConstrucao.MINA_FERRO, TipoRegiao.FLORESTA));
	}

	@ParameterizedTest
	@MethodSource("tabelaD9")
	void tabelaD9(TipoConstrucao tipo, BonusRegiao bonus, java.util.List<TipoRegiao> regioes) {
		assertEquals(java.util.Optional.ofNullable(bonus), ConstrucaoCatalogo.bonusRegiao(tipo), tipo.name());
		assertEquals(regioes, ConstrucaoCatalogo.regioesPermitidas(tipo), tipo.name());
	}

	static java.util.stream.Stream<Arguments> tabelaD9() {
		java.util.List<TipoRegiao> urb = java.util.List.of(TipoRegiao.URBANA);
		java.util.List<Arguments> l = new java.util.ArrayList<>();
		for (TipoConstrucao t : new TipoConstrucao[] {TipoConstrucao.CASA, TipoConstrucao.ARMAZEM, TipoConstrucao.FERRARIA,
				TipoConstrucao.ALFAIATARIA, TipoConstrucao.CARPINTARIA, TipoConstrucao.MERCADO, TipoConstrucao.ESTALAGEM,
				TipoConstrucao.QUARTEL}) {
			l.add(Arguments.of(t, null, urb));
		}
		for (TipoConstrucao t : new TipoConstrucao[] {TipoConstrucao.SERRARIA, TipoConstrucao.OLARIA, TipoConstrucao.FUNDICAO,
				TipoConstrucao.TECELAGEM, TipoConstrucao.CURTUME, TipoConstrucao.COZINHA}) {
			l.add(Arguments.of(t, BonusRegiao.INDUSTRIA, urb));
		}
		l.add(Arguments.of(TipoConstrucao.FAZENDA_PLANTIO, BonusRegiao.PLANTACOES, java.util.List.of(TipoRegiao.FLORESTA, TipoRegiao.PLANICIE)));
		l.add(Arguments.of(TipoConstrucao.FAZENDA_CRIACAO, BonusRegiao.CRIACOES, java.util.List.of(TipoRegiao.PLANICIE)));
		l.add(Arguments.of(TipoConstrucao.ACAMPAMENTO_LENHADORES, BonusRegiao.FLORESTA, java.util.List.of(TipoRegiao.FLORESTA, TipoRegiao.PLANICIE)));
		l.add(Arguments.of(TipoConstrucao.CABANA_CACA, BonusRegiao.FLORESTA, java.util.List.of(TipoRegiao.FLORESTA, TipoRegiao.PLANICIE)));
		l.add(Arguments.of(TipoConstrucao.BARREIRO, BonusRegiao.BARREIRO, java.util.List.of(TipoRegiao.FLORESTA)));
		l.add(Arguments.of(TipoConstrucao.PEDREIRA, BonusRegiao.ROCHA, java.util.List.of(TipoRegiao.MONTANHA)));
		l.add(Arguments.of(TipoConstrucao.MINA_FERRO, BonusRegiao.FERRO, java.util.List.of(TipoRegiao.MONTANHA)));
		l.add(Arguments.of(TipoConstrucao.MINA_CARVAO, BonusRegiao.CARVAO, java.util.List.of(TipoRegiao.MONTANHA)));
		l.add(Arguments.of(TipoConstrucao.SALINA, BonusRegiao.SALINAS, java.util.List.of(TipoRegiao.LITORAL)));
		l.add(Arguments.of(TipoConstrucao.MINA_ENXOFRE, BonusRegiao.ENXOFRE, java.util.List.of(TipoRegiao.LITORAL)));
		return l.stream();
	}

	@Test
	void jazidasEProfissoes() {
		assertEquals(Jazida.FLORESTA, ConstrucaoCatalogo.jazida(TipoConstrucao.ACAMPAMENTO_LENHADORES).orElseThrow());
		assertEquals(Jazida.FLORESTA, ConstrucaoCatalogo.jazida(TipoConstrucao.CABANA_CACA).orElseThrow());
		assertEquals(Jazida.VEIO_DE_FERRO, ConstrucaoCatalogo.jazida(TipoConstrucao.MINA_FERRO).orElseThrow());
		assertTrue(ConstrucaoCatalogo.jazida(TipoConstrucao.CASA).isEmpty());
		assertEquals(java.util.List.of(com.example.loginbase.jogo.cidadao.Profissao.COZINHEIRO, com.example.loginbase.jogo.cidadao.Profissao.COMERCIANTE), ConstrucaoCatalogo.profissoes(TipoConstrucao.ESTALAGEM));
		assertTrue(ConstrucaoCatalogo.profissoes(TipoConstrucao.CASA).isEmpty());
		for (TipoConstrucao t : TipoConstrucao.values()) {
			assertEquals(ConstrucaoCatalogo.ehPredioDeColeta(t), ConstrucaoCatalogo.jazida(t).isPresent(), t.name());
		}
	}
}
