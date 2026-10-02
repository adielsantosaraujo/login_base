package com.example.loginbase.jogo.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.CalculadoraPeEfetivo;
import com.example.loginbase.jogo.cidadao.EficienciaService;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@Transactional
class BonusFerramentaIntegrationTest {

	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired CalculadoraPeEfetivo calculadora;
	@Autowired EficienciaService eficienciaService;

	Vila vila;
	Familia familia;

	@BeforeEach
	void preparar() {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 7L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
	}

	private Cidadao cidadao(Profissao p, int peBase) {
		Cidadao c = new Cidadao(vila.getId(), familia.getId(), "T", Sexo.M, 30 * 12);
		c.setVit(0);
		c.setForca(0);
		c.setVel(0);
		c.setInteligencia(0);
		c.setCar(0);
		c = cidadaoRepository.saveAndFlush(c);
		if (peBase > 0) {
			profissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), p, peBase));
		}
		return c;
	}

	private Item equipar(Cidadao c, ItemSubtipo subtipo, SlotEquipamento slot, int nivel, BonusItem... bonus) {
		Item i = new Item(vila.getId(), subtipo, Qualidade.SIMPLES, nivel, List.of(bonus));
		i.setCidadaoId(c.getId());
		i.setSlot(slot);
		return itemRepository.saveAndFlush(i);
	}

	private double ef(Cidadao c, Profissao p) {
		return eficienciaService.eficiencia(c, p, 1.0, vila);
	}

	@Test
	void ferramentaCertaSomaNivel() {
		Cidadao c = cidadao(Profissao.AGRICULTOR, 2);
		equipar(c, ItemSubtipo.ENXADA, SlotEquipamento.FERRAMENTA, 3);
		assertThat(calculadora.calcular(c, Profissao.AGRICULTOR, 2)).isEqualTo(5);
		assertThat(ef(c, Profissao.AGRICULTOR)).isCloseTo(1.0, within(1e-9));
	}

	@Test
	void ferramentaDeOutraProfissaoNaoSoma() {
		Cidadao c = cidadao(Profissao.AGRICULTOR, 2);
		equipar(c, ItemSubtipo.PICARETA, SlotEquipamento.FERRAMENTA, 10);
		assertThat(calculadora.calcular(c, Profissao.AGRICULTOR, 2)).isEqualTo(2);
		assertThat(ef(c, Profissao.AGRICULTOR)).isCloseTo(0.7, within(1e-9));
		// PE base 0 + picareta L10: aplica a fórmula (0,5 + 0,1 x 10)
		assertThat(ef(c, Profissao.MINEIRO)).isCloseTo(1.5, within(1e-9));
	}

	@Test
	void intrinsecoProfDaFerramentaVale() {
		Cidadao c = cidadao(Profissao.AGRICULTOR, 2);
		equipar(c, ItemSubtipo.ENXADA, SlotEquipamento.FERRAMENTA, 1, new BonusItem(CodigoBonus.PROF, 2));
		assertThat(calculadora.calcular(c, Profissao.AGRICULTOR, 2)).isEqualTo(5);
	}

	@Test
	void profDeArmaVaiParaGuerreiro() {
		Cidadao c = cidadao(Profissao.GUERREIRO, 1);
		equipar(c, ItemSubtipo.ESPADA, SlotEquipamento.ARMA, 1, new BonusItem(CodigoBonus.PROF, 3));
		assertThat(calculadora.calcular(c, Profissao.GUERREIRO, 1)).isEqualTo(4);
		assertThat(calculadora.calcular(c, Profissao.AGRICULTOR, 1)).isEqualTo(1);
	}

	@Test
	void prodMultiplicaEficiencia() {
		Cidadao c = cidadao(Profissao.AGRICULTOR, 4);
		equipar(c, ItemSubtipo.ENXADA, SlotEquipamento.FERRAMENTA, 1, new BonusItem(CodigoBonus.PROD, 8));
		assertThat(ef(c, Profissao.AGRICULTOR)).isCloseTo(1.08, within(1e-9));
	}

	@Test
	void peBaseZeroComFerramentaAplicaFormula() {
		Cidadao c = cidadao(Profissao.AGRICULTOR, 0);
		assertThat(ef(c, Profissao.AGRICULTOR)).isCloseTo(0.5, within(1e-9));
		equipar(c, ItemSubtipo.ENXADA, SlotEquipamento.FERRAMENTA, 4);
		assertThat(ef(c, Profissao.AGRICULTOR)).isCloseTo(0.9, within(1e-9));
	}
}
