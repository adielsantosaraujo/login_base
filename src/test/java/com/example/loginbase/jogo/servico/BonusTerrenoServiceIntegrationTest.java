package com.example.loginbase.jogo.servico;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Ladrilho;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.TipoTerreno;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.LadrilhoRepository;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@Transactional
class BonusTerrenoServiceIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired LadrilhoRepository ladrilhoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired BonusTerrenoService service;

	private Vila vila;
	private Regiao regiao;
	private int proximoX;

	private void preparar() {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 123L, 1));
		Regiao r = new Regiao(vila.getId(), 6);
		r.setTipo(TipoRegiao.URBANA);
		r.setPossuida(true);
		regiao = regiaoRepository.saveAndFlush(r);
		proximoX = 0;
	}

	/** Cria um ladrilho-âncora (y = 0) e um prédio nele; devolve o prédio. */
	private Construcao predio(TipoConstrucao tipo, EstadoConstrucao estado, TipoTerreno terrenoAncora, int total) {
		int x = proximoX++;
		ladrilhoRepository.saveAndFlush(new Ladrilho(regiao.getId(), x, 0, terrenoAncora, total, 0));
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(tipo);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(6);
		c.setX(x);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(estado);
		c.setPoTotal(4);
		c.setPoAtual(estado == EstadoConstrucao.EM_OBRA ? 0 : 4);
		return construcaoRepository.saveAndFlush(c);
	}

	private Construcao ativa(TipoConstrucao tipo, TipoTerreno t, int total) {
		return predio(tipo, EstadoConstrucao.ATIVA, t, total);
	}

	@Test
	void comercioMediaDeMercadoEEstalagem() {
		preparar();
		ativa(TipoConstrucao.MERCADO, TipoTerreno.COMERCIO, 45);
		ativa(TipoConstrucao.ESTALAGEM, TipoTerreno.COMERCIO, 55);
		assertThat(service.media(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualByComparingTo("50");
		assertThat(service.fator(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualTo(new BigDecimal("1.5000"));
	}

	@Test
	void predioForaDoTerrenoNaoEntraNaMedia() {
		preparar();
		ativa(TipoConstrucao.MERCADO, TipoTerreno.INDUSTRIA, 90);
		ativa(TipoConstrucao.ESTALAGEM, TipoTerreno.COMERCIO, 55);
		assertThat(service.media(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualByComparingTo("55");
	}

	@Test
	void casasDesenvolvimentoMedia() {
		preparar();
		for (int v : new int[] { 20, 40, 60, 80 }) {
			ativa(TipoConstrucao.CASA, TipoTerreno.DESENVOLVIMENTO, v);
		}
		assertThat(service.media(vila.getId(), GrupoBonusVila.DESENVOLVIMENTO)).isEqualByComparingTo("50");
	}

	@Test
	void quarteisMilitarIgnoraForaDoTerreno() {
		preparar();
		ativa(TipoConstrucao.QUARTEL, TipoTerreno.MILITAR, 45);
		ativa(TipoConstrucao.QUARTEL, TipoTerreno.MILITAR, 35);
		ativa(TipoConstrucao.QUARTEL, TipoTerreno.INDUSTRIA, 99);
		assertThat(service.media(vila.getId(), GrupoBonusVila.MILITAR)).isEqualByComparingTo("40");
	}

	@Test
	void nenhumNoTerrenoDaZero() {
		preparar();
		ativa(TipoConstrucao.MERCADO, TipoTerreno.INDUSTRIA, 90);
		assertThat(service.media(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualByComparingTo("0");
		assertThat(service.fator(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualTo(new BigDecimal("1.0000"));
		assertThat(service.media(vila.getId(), GrupoBonusVila.MILITAR)).isEqualByComparingTo("0");
	}

	@Test
	void predioEmObraNaoEntra() {
		preparar();
		predio(TipoConstrucao.MERCADO, EstadoConstrucao.EM_OBRA, TipoTerreno.COMERCIO, 90);
		ativa(TipoConstrucao.ESTALAGEM, TipoTerreno.COMERCIO, 30);
		predio(TipoConstrucao.ESTALAGEM, EstadoConstrucao.EM_UPGRADE, TipoTerreno.COMERCIO, 50);
		assertThat(service.media(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualByComparingTo("40");
	}

	@Test
	void predioNoTerrenoCertoComZeroEntraComZero() {
		preparar();
		ativa(TipoConstrucao.MERCADO, TipoTerreno.COMERCIO, 0);
		ativa(TipoConstrucao.ESTALAGEM, TipoTerreno.COMERCIO, 60);
		assertThat(service.media(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualByComparingTo("30");
	}

	@Test
	void mediaComDuasCasasArredondaHalfUpEFatorQuatroCasas() {
		preparar();
		ativa(TipoConstrucao.MERCADO, TipoTerreno.COMERCIO, 45);
		ativa(TipoConstrucao.ESTALAGEM, TipoTerreno.COMERCIO, 46);
		assertThat(service.media(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualTo(new BigDecimal("45.50"));
		assertThat(service.fator(vila.getId(), GrupoBonusVila.COMERCIO)).isEqualTo(new BigDecimal("1.4550"));
	}

	@Test
	void bonusDoPredioAncoraCerta() {
		preparar();
		Construcao c = ativa(TipoConstrucao.ACAMPAMENTO_LENHADORES, TipoTerreno.FLORESTA, 80);
		assertThat(service.bonusDoPredio(vila.getId(), c)).isEqualTo(80);
		assertThat(service.fatorDoPredio(vila.getId(), c)).isEqualTo(new BigDecimal("1.80"));
	}

	@Test
	void bonusDoPredioAncoraErrada() {
		preparar();
		Construcao c = ativa(TipoConstrucao.ACAMPAMENTO_LENHADORES, TipoTerreno.BARREIRO, 80);
		assertThat(service.bonusDoPredio(vila.getId(), c)).isZero();
		assertThat(service.fatorDoPredio(vila.getId(), c)).isEqualTo(new BigDecimal("1.00"));
	}

	@Test
	void bonusDoPredioSemTerrenoELadrilhoAusente() {
		preparar();
		Construcao armazem = ativa(TipoConstrucao.ARMAZEM, TipoTerreno.INDUSTRIA, 70);
		assertThat(service.bonusDoPredio(vila.getId(), armazem)).isZero();

		Construcao fantasma = new Construcao();
		fantasma.setTipo(TipoConstrucao.SERRARIA);
		fantasma.setRegiaoIndice(6);
		fantasma.setX(9);
		fantasma.setY(9);
		assertThat(service.bonusDoPredio(vila.getId(), fantasma)).isZero();
		fantasma.setRegiaoIndice(15);
		assertThat(service.bonusDoPredio(vila.getId(), fantasma)).isZero();
	}

}
