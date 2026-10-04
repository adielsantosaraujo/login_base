package com.example.loginbase.jogo.cidadao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.loginbase.jogo.cidadao.DistribuicaoPopulacao.DistribuicaoPessoa;
import com.example.loginbase.jogo.cidadao.DistribuicaoPopulacao.FamiliaEntrada;
import com.example.loginbase.jogo.cidadao.DistribuicaoPopulacao.PessoaEntrada;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class DistribuicaoPopulacaoTest {

	private static JsonNode fixture;
	private static List<FamiliaEntrada> familias;

	@BeforeAll
	static void carregar() throws Exception {
		fixture = new ObjectMapper().readTree(
				Path.of("frontend/src/domain/__fixtures__/distribuicao-populacao.json").toFile());
		familias = new ArrayList<>();
		for (JsonNode f : fixture.get("familias")) {
			List<PessoaEntrada> membros = new ArrayList<>();
			for (JsonNode c : f.get("cidadaos")) {
				membros.add(new PessoaEntrada(c.get("nome").asString(), Sexo.valueOf(c.get("sexo").asString()),
						c.get("idadeAnos").asInt(), PapelFamiliar.valueOf(c.get("papel").asString())));
			}
			familias.add(new FamiliaEntrada(membros));
		}
	}

	private static void confere(DistribuicaoPessoa obtido, JsonNode esperado) {
		for (Caracteristica c : Caracteristica.values()) {
			assertThat(obtido.caracteristicas().get(c)).as(c.name()).isEqualTo(esperado.get("caracteristicas").get(c.name()).asInt());
		}
		for (Profissao p : Profissao.values()) {
			assertThat(obtido.profissoes().get(p)).as(p.name()).isEqualTo(esperado.get("profissoes").get(p.name()).asInt());
		}
	}

	private static Map<Profissao, Integer> plano(JsonNode n) {
		Map<Profissao, Integer> m = new EnumMap<>(Profissao.class);
		for (Profissao p : Profissao.values()) {
			m.put(p, n.has(p.name()) ? n.get(p.name()).asInt() : 0);
		}
		return m;
	}

	@Test
	void reproduzVetoresDasPessoas() {
		for (Profissao p : Profissao.values()) {
			confere(DistribuicaoPopulacao.distribuirPessoa(p), fixture.get("pessoas").get(p.name()));
		}
	}

	@Test
	void reproduzCasosDaFixture() {
		assertThat(fixture.get("casos").size()).isEqualTo(3);
		for (JsonNode caso : fixture.get("casos")) {
			List<List<DistribuicaoPessoa>> r = DistribuicaoPopulacao.distribuirPopulacao(familias, plano(caso.get("plano")));
			for (int f = 0; f < r.size(); f++) {
				for (int m = 0; m < r.get(f).size(); m++) {
					JsonNode esp = caso.get("resultado").get(f).get(m);
					confere(r.get(f).get(m), esp);
					assertThat(DistribuicaoPopulacao.principal(r.get(f).get(m).profissoes()).name())
							.isEqualTo(esp.get("principal").asString());
				}
			}
			assertThat(DistribuicaoPopulacao.familiaLiderSugerida(familias, r))
					.isEqualTo(caso.get("familiaLiderSugerida").asInt());
		}
	}

	@Test
	void planoPadrao() {
		List<List<DistribuicaoPessoa>> r = DistribuicaoPopulacao.distribuirPopulacao(familias,
				DistribuicaoPopulacao.PLANO_PADRAO);
		DistribuicaoPessoa pai = r.get(0).get(0);
		assertThat(DistribuicaoPopulacao.principal(pai.profissoes())).isEqualTo(Profissao.COMERCIANTE);
		assertThat(pai.caracteristicas()).containsEntry(Caracteristica.VIT, 1).containsEntry(Caracteristica.FOR, 1)
				.containsEntry(Caracteristica.VEL, 5).containsEntry(Caracteristica.INT, 0)
				.containsEntry(Caracteristica.CAR, 13);
		assertThat(pai.profissoes()).containsEntry(Profissao.COMERCIANTE, 5).containsEntry(Profissao.COZINHEIRO, 3)
				.containsEntry(Profissao.CARREGADOR, 2);
		assertThat(DistribuicaoPopulacao.familiaLiderSugerida(familias, r)).isZero();
	}

	@Test
	void principalDesempataPelaOrdemDoEnum() {
		EnumMap<Profissao, Integer> m = new EnumMap<>(Profissao.class);
		m.put(Profissao.MINEIRO, 4);
		m.put(Profissao.CARREGADOR, 4);
		assertThat(DistribuicaoPopulacao.principal(m)).isEqualTo(Profissao.CARREGADOR);
		assertThat(DistribuicaoPopulacao.principal(new EnumMap<>(Profissao.class))).isNull();
	}

	@Test
	void liderDesempataPelaOrdemDoPapel() {
		FamiliaEntrada f = new FamiliaEntrada(List.of(
				new PessoaEntrada("a", Sexo.F, 40, PapelFamiliar.MAE),
				new PessoaEntrada("b", Sexo.M, 40, PapelFamiliar.PAI),
				new PessoaEntrada("c", Sexo.M, 10, PapelFamiliar.FILHO)));
		assertThat(DistribuicaoPopulacao.liderDaFamilia(f)).isEqualTo(1);
		FamiliaEntrada g = new FamiliaEntrada(List.of(
				new PessoaEntrada("a", Sexo.F, 40, PapelFamiliar.MAE),
				new PessoaEntrada("b", Sexo.M, 45, PapelFamiliar.PAI)));
		assertThat(DistribuicaoPopulacao.liderDaFamilia(g)).isEqualTo(1);
	}

	@Test
	void bonusLider() {
		assertThat(DistribuicaoPopulacao.bonusLider(13)).isEqualTo(6);
		assertThat(DistribuicaoPopulacao.bonusLider(25)).isEqualTo(10);
	}

	@Test
	void planoComSomaInvalidaLanca() {
		Map<Profissao, Integer> p = new EnumMap<>(Profissao.class);
		p.put(Profissao.CONSTRUTOR, 15);
		assertThatThrownBy(() -> DistribuicaoPopulacao.distribuirPopulacao(familias, p))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void deterministico() {
		var a = DistribuicaoPopulacao.distribuirPopulacao(familias, DistribuicaoPopulacao.PLANO_PADRAO);
		var b = DistribuicaoPopulacao.distribuirPopulacao(familias, DistribuicaoPopulacao.PLANO_PADRAO);
		assertThat(a).isEqualTo(b);
	}

	@Test
	void validarPlanoPadraoOk() {
		var r = DistribuicaoPopulacao.distribuirPopulacao(familias, DistribuicaoPopulacao.PLANO_PADRAO);
		assertThat(DistribuicaoPopulacao.validar(r.stream().flatMap(List::stream).toList())).isEmpty();
	}
}
