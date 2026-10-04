package com.example.loginbase.jogo.cidadao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
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
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.repositorio.VilaRepository;
import com.example.loginbase.jogo.servico.MapaTestes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PopulacaoControllerIntegrationTest {

	private static final ObjectMapper JSON = new ObjectMapper();

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired FamiliaRepository familiaRepository;
	@Autowired CidadaoRepository cidadaoRepository;
	@Autowired CidadaoProfissaoRepository profissaoRepository;

	private Usuario usuarioComVila() throws Exception {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		String previa = mvc.perform(post("/api/jogo/vila/previa").with(user(u.getEmail()).roles("USER")).with(csrf()))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		JsonNode no = JSON.readTree(previa);
		List<TipoRegiao> tipos = new ArrayList<>();
		no.get("regioes").forEach(r -> tipos.add(TipoRegiao.valueOf(r.get("tipo").asText())));
		List<Integer> indices = MapaTestes.selecionePorTipos(tipos, true);
		String corpo = "{\"previaId\":\"" + no.get("previaId").asText() + "\",\"indices\":" + indices + "}";
		mvc.perform(post("/api/jogo/vila").with(user(u.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isCreated());
		return u;
	}

	private ResultActions obter(Usuario u) throws Exception {
		return mvc.perform(get("/api/jogo/vila/populacao").with(user(u.getEmail()).roles("USER")));
	}

	private ResultActions confirmar(Usuario u, String corpo) throws Exception {
		return mvc.perform(post("/api/jogo/vila/populacao").with(user(u.getEmail()).roles("USER")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(corpo));
	}

	/** Corpo do POST montado a partir da sugestão do GET (16 cidadãos, mínimos atendidos). */
	private ObjectNode corpoSugerido(Usuario u) throws Exception {
		JsonNode g = JSON.readTree(obter(u).andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString());
		ObjectNode corpo = JSON.createObjectNode();
		corpo.put("familiaLiderId", g.get("familiaLiderSugeridaId").asLong());
		ArrayNode fams = corpo.putArray("familias");
		for (JsonNode f : g.get("familias")) {
			ObjectNode fo = fams.addObject();
			fo.put("familiaId", f.get("familiaId").asLong());
			ArrayNode cs = fo.putArray("cidadaos");
			for (JsonNode c : f.get("cidadaos")) {
				ObjectNode co = cs.addObject();
				co.put("cidadaoId", c.get("cidadaoId").asLong());
				co.set("caracteristicas", c.get("caracteristicas"));
				co.set("profissoes", c.get("profissoes"));
			}
		}
		return corpo;
	}

	private static ObjectNode cidadao(ObjectNode corpo, int fam, int idx) {
		return (ObjectNode) corpo.get("familias").get(fam).get("cidadaos").get(idx);
	}

	private ResultActions confirmar(Usuario u, ObjectNode corpo) throws Exception {
		return confirmar(u, JSON.writeValueAsString(corpo));
	}

	private boolean confirmada(Usuario u) {
		return vilaRepository.findByUsuarioId(u.getId()).orElseThrow().isPopulacaoConfirmada();
	}

	@Test
	void obterDevolveSugestaoDoPlanoPadrao() throws Exception {
		Usuario u = usuarioComVila();
		var vila = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		Familia primeira = familiaRepository.findByVilaId(vila.getId()).stream()
				.min(java.util.Comparator.comparing(Familia::getId)).orElseThrow();
		obter(u).andExpect(status().isOk())
				.andExpect(jsonPath("$.populacaoConfirmada").value(false))
				.andExpect(jsonPath("$.familiaLiderId").doesNotExist())
				.andExpect(jsonPath("$.familiaLiderSugeridaId").value(primeira.getId()))
				.andExpect(jsonPath("$.limites.caracteristicasTotal").value(20))
				.andExpect(jsonPath("$.limites.profissoesTotal").value(10))
				.andExpect(jsonPath("$.minimos.CONSTRUTOR").value(2))
				.andExpect(jsonPath("$.minimos.CARREGADOR").value(2))
				.andExpect(jsonPath("$.plano.COMERCIANTE").value(1))
				.andExpect(jsonPath("$.familias.length()").value(4))
				.andExpect(jsonPath("$.familias[0].familiaId").value(primeira.getId()))
				.andExpect(jsonPath("$.familias[0].cidadaos.length()").value(4))
				.andExpect(jsonPath("$.familias[0].cidadaos[0].papel").value("PAI"))
				.andExpect(jsonPath("$.familias[0].cidadaos[1].papel").value("MAE"))
				.andExpect(jsonPath("$.familias[0].cidadaos[2].papel").value("FILHO"))
				.andExpect(jsonPath("$.familias[0].cidadaos[3].papel").value("FILHA"))
				.andExpect(jsonPath("$.familias[0].cidadaos[0].profissoes.COMERCIANTE").value(5))
				.andExpect(jsonPath("$.familias[0].cidadaos[0].caracteristicas.CAR").value(13))
				.andExpect(jsonPath("$.familias[0].cidadaos[0].profissoes.length()").value(12))
				.andExpect(jsonPath("$.familias[0].cidadaos[0].caracteristicas.length()").value(5))
				.andExpect(jsonPath("$.familias[0].cidadaos[0].caracteristicas.VIT").exists())
				.andExpect(jsonPath("$.familias[0].cidadaos[0].caracteristicas.FOR").exists())
				.andExpect(jsonPath("$.familias[0].cidadaos[0].caracteristicas.VEL").exists())
				.andExpect(jsonPath("$.familias[0].cidadaos[0].caracteristicas.INT").exists());
		mvc.perform(get("/api/jogo/vila").with(user(u.getEmail()).roles("USER")))
				.andExpect(jsonPath("$.populacaoConfirmada").value(false));
	}

	@Test
	void duasConsultasDevolvemJsonIdentico() throws Exception {
		Usuario u = usuarioComVila();
		String a = obter(u).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String b = obter(u).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(a, b);
	}

	@Test
	void obterConfirmadaDevolveValoresGravadosELider() throws Exception {
		Usuario u = usuarioComVila();
		var vila = vilaRepository.findByUsuarioId(u.getId()).orElseThrow();
		Familia f = familiaRepository.findByVilaId(vila.getId()).get(1);
		Cidadao pai = cidadaoRepository.findByFamiliaIdAndVivoTrue(f.getId()).stream()
				.filter(c -> c.getPaiId() == null && c.getSexo() == Sexo.M).findFirst().orElseThrow();
		pai.setCar(7);
		pai.setVit(3);
		cidadaoRepository.saveAndFlush(pai);
		profissaoRepository.saveAndFlush(new CidadaoProfissao(pai.getId(), Profissao.FERREIRO, 4));
		vila.setPopulacaoConfirmada(true);
		vila.setFamiliaLiderId(f.getId());
		vilaRepository.saveAndFlush(vila);
		obter(u).andExpect(status().isOk())
				.andExpect(jsonPath("$.populacaoConfirmada").value(true))
				.andExpect(jsonPath("$.familiaLiderId").value(f.getId()))
				.andExpect(jsonPath("$.familias[1].cidadaos[0].cidadaoId").value(pai.getId()))
				.andExpect(jsonPath("$.familias[1].cidadaos[0].caracteristicas.CAR").value(7))
				.andExpect(jsonPath("$.familias[1].cidadaos[0].caracteristicas.VIT").value(3))
				.andExpect(jsonPath("$.familias[1].cidadaos[0].profissoes.FERREIRO").value(4))
				.andExpect(jsonPath("$.familias[1].cidadaos[0].profissoes.COMERCIANTE").value(0))
				.andExpect(jsonPath("$.familias[1].cidadaos[0].profissoes.length()").value(12));
	}

	@Test
	void obterSemVilaDevolve404() throws Exception {
		Usuario u = new Usuario();
		u.setNome("Sem vila");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		obter(u).andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("VILA_NAO_ENCONTRADA"));
	}

	@Test
	void confirmaSugestaoComBonusDoLider() throws Exception {
		Usuario u = usuarioComVila();
		ObjectNode corpo = corpoSugerido(u);
		long lider = corpo.get("familiaLiderId").asLong();
		// um cidadão com 15/20 de característica
		ObjectNode c0 = cidadao(corpo, 1, 2);
		long id = c0.get("cidadaoId").asLong();
		ObjectNode car = (ObjectNode) c0.get("caracteristicas");
		car.put("VIT", 0).put("FOR", 0).put("VEL", 0).put("INT", 0).put("CAR", 0).put("VIT", 15);
		confirmar(u, corpo).andExpect(status().isOk())
				.andExpect(jsonPath("$.bonusLider").value(6))
				.andExpect(jsonPath("$.familiaLiderId").value(lider))
				.andExpect(jsonPath("$.proximaEtapa").value("MAPA"));
		Cidadao salvo = cidadaoRepository.findById(id).orElseThrow();
		assertEquals(15, salvo.getVit());
		assertEquals(5, salvo.getPontosCarPendentes());
		assertTrue(confirmada(u));
		assertEquals(lider, vilaRepository.findByUsuarioId(u.getId()).orElseThrow().getFamiliaLiderId());
		long comZero = profissaoRepository.findByCidadaoId(id).stream().filter(p -> p.getPontosBase() <= 0).count();
		assertEquals(0, comZero);
		confirmar(u, corpo).andExpect(status().isConflict())
				.andExpect(jsonPath("$.codigo").value("POPULACAO_JA_CONFIRMADA"));
	}

	@Test
	void aceitaAtributoConcentrado() throws Exception {
		Usuario u = usuarioComVila();
		ObjectNode corpo = corpoSugerido(u);
		ObjectNode c = cidadao(corpo, 0, 3);
		ObjectNode car = (ObjectNode) c.get("caracteristicas");
		car.put("VIT", 20).put("FOR", 0).put("VEL", 0).put("INT", 0).put("CAR", 0);
		ObjectNode prof = (ObjectNode) c.get("profissoes");
		for (var k : new ArrayList<>(prof.propertyNames())) {
			prof.put(k, 0);
		}
		prof.put("CONSTRUTOR", 10);
		confirmar(u, corpo).andExpect(status().isOk());
		assertEquals(20, cidadaoRepository.findById(c.get("cidadaoId").asLong()).orElseThrow().getVit());
	}

	@Test
	void recusaLimiteDeCaracteristicasSemGravar() throws Exception {
		Usuario u = usuarioComVila();
		ObjectNode corpo = corpoSugerido(u);
		long id = cidadao(corpo, 0, 0).get("cidadaoId").asLong();
		int vitAntes = cidadaoRepository.findById(id).orElseThrow().getVit();
		((ObjectNode) cidadao(corpo, 1, 0).get("caracteristicas")).put("VIT", 21).put("CAR", 0).put("FOR", 0)
				.put("VEL", 0).put("INT", 0);
		((ObjectNode) cidadao(corpo, 0, 0).get("caracteristicas")).put("VIT", 7);
		confirmar(u, corpo).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("LIMITE_CARACTERISTICAS"));
		assertEquals(vitAntes, cidadaoRepository.findById(id).orElseThrow().getVit());
		assertEquals(false, confirmada(u));
	}

	@Test
	void recusaLimiteDeProfissoes() throws Exception {
		Usuario u = usuarioComVila();
		ObjectNode corpo = corpoSugerido(u);
		((ObjectNode) cidadao(corpo, 0, 0).get("profissoes")).put("MINEIRO", 11);
		confirmar(u, corpo).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("LIMITE_PROFISSOES"));
	}

	@Test
	void recusaPontosInvalidosEPopulacaoIncompleta() throws Exception {
		Usuario u = usuarioComVila();
		ObjectNode corpo = corpoSugerido(u);
		((ObjectNode) cidadao(corpo, 0, 0).get("caracteristicas")).put("VIT", -1);
		confirmar(u, corpo).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("PONTOS_INVALIDOS"));
		ObjectNode c2 = corpoSugerido(u);
		((ObjectNode) cidadao(c2, 0, 0).get("caracteristicas")).put("XYZ", 1);
		confirmar(u, c2).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("PONTOS_INVALIDOS"));
		ObjectNode c3 = corpoSugerido(u);
		((ArrayNode) c3.get("familias").get(3).get("cidadaos")).remove(0);
		confirmar(u, c3).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("POPULACAO_INCOMPLETA"));
		ObjectNode c4 = corpoSugerido(u);
		c4.putNull("familiaLiderId");
		confirmar(u, c4).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codigo").value("FAMILIA_LIDER_OBRIGATORIA"));
		assertEquals(false, confirmada(u));
	}

	@Test
	void recusaMinimoDeConstrutoresECarregadores() throws Exception {
		Usuario u = usuarioComVila();
		for (String prof : new String[] { "CONSTRUTOR", "CARREGADOR" }) {
			ObjectNode corpo = corpoSugerido(u);
			List<ObjectNode> principais = new ArrayList<>();
			for (JsonNode f : corpo.get("familias")) {
				for (JsonNode c : f.get("cidadaos")) {
					ObjectNode p = (ObjectNode) c.get("profissoes");
					if (DistribuicaoPopulacao.principal(paraMapa(p)) == Profissao.valueOf(prof)) {
						principais.add(p);
					}
				}
			}
			principais.stream().skip(1).forEach(p -> p.put(prof, 0));
			confirmar(u, corpo).andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.codigo").value("MINIMO_" + (prof.equals("CONSTRUTOR") ? "CONSTRUTORES"
							: "CARREGADORES")));
		}
	}

	private static java.util.Map<Profissao, Integer> paraMapa(ObjectNode p) {
		java.util.EnumMap<Profissao, Integer> m = new java.util.EnumMap<>(Profissao.class);
		p.propertyNames().forEach(k -> m.put(Profissao.valueOf(k), p.get(k).asInt()));
		return m;
	}
}
