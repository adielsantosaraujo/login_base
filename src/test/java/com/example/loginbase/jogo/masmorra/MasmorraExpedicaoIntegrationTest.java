package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.batalha.BatalhaRepository;
import com.example.loginbase.jogo.cidadao.Cidadao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissao;
import com.example.loginbase.jogo.cidadao.CidadaoProfissaoRepository;
import com.example.loginbase.jogo.cidadao.CidadaoRepository;
import com.example.loginbase.jogo.cidadao.EstadoCidadao;
import com.example.loginbase.jogo.cidadao.Familia;
import com.example.loginbase.jogo.cidadao.FamiliaRepository;
import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.cidadao.Sexo;
import com.example.loginbase.jogo.construcao.Construcao;
import com.example.loginbase.jogo.construcao.ConstrucaoRepository;
import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.Item;
import com.example.loginbase.jogo.item.ItemRepository;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.item.Qualidade;
import com.example.loginbase.jogo.item.SlotEquipamento;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.turno.EtapaTurno;
import com.example.loginbase.jogo.turno.EventoTurnoRepository;
import com.example.loginbase.jogo.turno.TipoEventoTurno;
import com.example.loginbase.jogo.quartel.EstadoTropa;
import com.example.loginbase.jogo.quartel.PosicaoTropa;
import com.example.loginbase.jogo.quartel.Tropa;
import com.example.loginbase.jogo.quartel.TropaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MasmorraExpedicaoIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired ItemRepository itemRepository;
	@Autowired TropaRepository tropaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired BatalhaRepository batalhaRepository;
	@Autowired EventoTurnoRepository eventoRepository;
	@Autowired List<EtapaTurno> etapas;
	@Autowired MasmorraRepository masmorraRepository;

	private Usuario usuario;
	private Vila vila;
	private Familia familia;
	private Construcao quartel;
	private int seq;

	private void cenario(String refeicao) {
		Usuario u = new Usuario();
		u.setNome("J");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		usuario = usuarioRepository.saveAndFlush(u);
		vila = vilaRepository.saveAndFlush(new Vila(usuario.getId(), "Vila", 1L, 1));
		familia = familiaRepository.saveAndFlush(new Familia(vila.getId(), "Silva", null));
		Regiao r = new Regiao(vila.getId(), 1);
		r.setPossuida(true);
		regiaoRepository.saveAndFlush(r);
		estoqueService.inicializar(vila, Map.of(Recurso.REFEICAO, new BigDecimal(refeicao)));
		Construcao c = new Construcao();
		c.setVilaId(vila.getId());
		c.setTipo(TipoConstrucao.QUARTEL);
		c.setNivel(NivelConstrucao.N1);
		c.setRegiaoIndice(1);
		c.setX(0);
		c.setY(0);
		c.setTamanho(1);
		c.setEstado(EstadoConstrucao.ATIVA);
		c.setPoTotal(8);
		c.setPoAtual(8);
		quartel = construcaoRepository.saveAndFlush(c);
	}

	private Tropa tropa(int membros, boolean forte) {
		Tropa t = tropaRepository.saveAndFlush(new Tropa(vila.getId(), quartel.getId(), "T" + (++seq)));
		for (int i = 0; i < membros; i++) {
			Cidadao c = new Cidadao(vila.getId(), familia.getId(), "G" + (++seq), Sexo.M, 20 * 12);
			c.setVit(forte ? 40 : 1);
			c.setForca(forte ? 40 : 1);
			c.setVel(5);
			c.setTropaId(t.getId());
			c.setPosicaoTropa(PosicaoTropa.FRENTE);
			c = cidadaoRepository.saveAndFlush(c);
			profissaoRepository.saveAndFlush(new CidadaoProfissao(c.getId(), Profissao.GUERREIRO, 5));
			Item espada = new Item(vila.getId(), ItemSubtipo.ESPADA, Qualidade.SIMPLES, 1, List.of());
			espada.setCidadaoId(c.getId());
			espada.setSlot(SlotEquipamento.ARMA);
			itemRepository.saveAndFlush(espada);
		}
		return t;
	}

	private Masmorra masmorra(int regiao, int nivel) {
		Regiao r = new Regiao(vila.getId(), regiao);
		regiaoRepository.saveAndFlush(r);
		return masmorraRepository.saveAndFlush(new Masmorra(vila.getId(), regiao, nivel, 1));
	}

	private ResultActions enviar(Long tropaId, Long masmorraId) throws Exception {
		return mvc.perform(post("/api/jogo/tropas/" + tropaId + "/expedicao").with(user(usuario.getEmail()).roles("USER"))
				.with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"masmorraId\":" + masmorraId + "}"));
	}

	private void turno(int ordem, int numero) {
		etapas.stream().filter(e -> e.ordem() == ordem).findFirst().orElseThrow().executar(vila, numero);
	}

	private Tropa relida(Tropa t) {
		return tropaRepository.findById(t.getId()).orElseThrow();
	}

	private long eventos(TipoEventoTurno tipo) {
		return eventoRepository.findAll().stream().filter(e -> e.getVilaId().equals(vila.getId()) && e.getTipo() == tipo)
				.count();
	}

	@Test
	void vitoriaDestroiMasmorraBloqueiaRegiaoETropaVolta() throws Exception {
		cenario("60");
		Tropa t = tropa(6, true);
		Masmorra m = masmorra(3, 2); // região 3 -> distância 2
		enviar(t.getId(), m.getId()).andExpect(status().isCreated());

		turno(8, 10);
		assertThat(batalhaRepository.findAll()).isEmpty();
		assertThat(masmorraRepository.findById(m.getId()).orElseThrow().isAtiva()).isTrue();

		turno(8, 11);
		assertThat(batalhaRepository.findAll()).hasSize(1);
		Masmorra depois = masmorraRepository.findById(m.getId()).orElseThrow();
		assertThat(depois.isAtiva()).isFalse();
		assertThat(depois.getTurnoUltimoAtaque()).isEqualTo(11);
		assertThat(regiaoRepository.findByVilaIdAndIndice(vila.getId(), 3).orElseThrow().getLimpaAteTurno())
				.isEqualTo(17);
		assertThat(relida(t).getEstado()).isEqualTo(EstadoTropa.EM_VIAGEM_VOLTA);
		assertThat(eventos(TipoEventoTurno.MASMORRA)).isEqualTo(1);

		turno(8, 12);
		assertThat(relida(t).getEstado()).isEqualTo(EstadoTropa.EM_VIAGEM_VOLTA);
		turno(8, 13);
		assertThat(relida(t).getEstado()).isEqualTo(EstadoTropa.AQUARTELADA);
	}

	@Test
	void derrotaMantemMasmorraAtivaESemNivelAlterado() throws Exception {
		cenario("60");
		Tropa t = tropa(1, false);
		Masmorra m = masmorra(3, 5);
		m.setTurnosSemAtaque(4);
		masmorraRepository.saveAndFlush(m);
		enviar(t.getId(), m.getId()).andExpect(status().isCreated());

		turno(8, 10);
		turno(8, 11);

		assertThat(batalhaRepository.findAll()).hasSize(1);
		Masmorra depois = masmorraRepository.findById(m.getId()).orElseThrow();
		assertThat(depois.isAtiva()).isTrue();
		assertThat(depois.getNivel()).isEqualTo(5);
		assertThat(depois.getTurnosSemAtaque()).isZero();
		assertThat(depois.getTurnoUltimoAtaque()).isEqualTo(11);
		assertThat(regiaoRepository.findByVilaIdAndIndice(vila.getId(), 3).orElseThrow().getLimpaAteTurno()).isNull();
		assertThat(eventos(TipoEventoTurno.MASMORRA)).isEqualTo(1);
	}
}
