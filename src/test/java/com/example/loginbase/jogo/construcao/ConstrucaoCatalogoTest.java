package com.example.loginbase.jogo.construcao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

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
		assertEquals(TipoRegiao.URBANA, ConstrucaoCatalogo.regiaoPermitida(TipoConstrucao.QUARTEL));
		assertFalse(ConstrucaoCatalogo.permiteRegiao(TipoConstrucao.QUARTEL, TipoRegiao.RURAL));
		assertTrue(ConstrucaoCatalogo.permiteRegiao(TipoConstrucao.FAZENDA_PLANTIO, TipoRegiao.RURAL));
		assertTrue(ConstrucaoCatalogo.permiteRegiao(TipoConstrucao.PEDREIRA, TipoRegiao.COLETA));
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
