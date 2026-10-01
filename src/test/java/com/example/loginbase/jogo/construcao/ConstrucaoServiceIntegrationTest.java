package com.example.loginbase.jogo.construcao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.comum.JogoException;
import com.example.loginbase.jogo.excecao.RecursosInsuficientesException;
import com.example.loginbase.jogo.modelo.Regiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.recurso.EstoqueService;
import com.example.loginbase.jogo.recurso.Recurso;
import com.example.loginbase.jogo.repositorio.RegiaoRepository;
import com.example.loginbase.jogo.repositorio.VilaRepository;

@SpringBootTest
@Transactional
class ConstrucaoServiceIntegrationTest {

	@Autowired ConstrucaoService service;
	@Autowired ConstrucaoRepository construcaoRepository;
	@Autowired UsuarioRepository usuarioRepository;
	@Autowired VilaRepository vilaRepository;
	@Autowired RegiaoRepository regiaoRepository;
	@Autowired EstoqueService estoqueService;
	@Autowired VerificadorOcupacaoLadrilhos verificador;

	/** Regiões 1 (URBANA) e 2 (RURAL) possuídas; as demais não. */
	private Vila vila(String madeira, String pedra, String argila) {
		Usuario u = new Usuario();
		u.setNome("Jogador");
		u.setEmail(UUID.randomUUID() + "@teste.com");
		u.setSenha("x");
		u = usuarioRepository.saveAndFlush(u);
		Vila vila = vilaRepository.saveAndFlush(new Vila(u.getId(), "Vila", 42L, 1));
		for (int i = 1; i <= 16; i++) {
			Regiao r = new Regiao(vila.getId(), i);
			if (i <= 2) {
				r.setPossuida(true);
				r.setTipo(i == 1 ? TipoRegiao.URBANA : TipoRegiao.RURAL);
			}
			regiaoRepository.save(r);
		}
		regiaoRepository.flush();
		estoqueService.inicializar(vila, Map.of(Recurso.MADEIRA, new BigDecimal(madeira),
				Recurso.PEDRA, new BigDecimal(pedra), Recurso.ARGILA, new BigDecimal(argila)));
		return vila;
	}

	@Test
	void criarCasaN1Sucesso() {
		Vila vila = vila("100", "100", "100");
		Construcao c = service.criar(vila, TipoConstrucao.CASA, 1, 3, 4);
		assertThat(c.getId()).isNotNull();
		assertThat(c.getEstado()).isEqualTo(EstadoConstrucao.EM_OBRA);
		assertThat(c.getNivel()).isEqualTo(NivelConstrucao.N1);
		assertThat(c.getPoTotal()).isEqualTo(4);
		assertThat(c.getPoAtual()).isZero();
		assertThat(c.getTamanho()).isEqualTo(1);
		assertThat(estoqueService.quantidade(vila, Recurso.MADEIRA)).isEqualByComparingTo("80");
		assertThat(estoqueService.quantidade(vila, Recurso.PEDRA)).isEqualByComparingTo("90");
		assertThat(estoqueService.quantidade(vila, Recurso.ARGILA)).isEqualByComparingTo("90");
		assertThat(verificador.ocupado(vila.getId(), 1, 3, 4)).isTrue();
		assertThat(verificador.ocupado(vila.getId(), 1, 0, 0)).isFalse();
	}

	@Test
	void quartelEmRegiaoRuralRejeitado() {
		Vila vila = vila("100", "100", "100");
		assertThatThrownBy(() -> service.criar(vila, TipoConstrucao.QUARTEL, 2, 0, 0))
				.isInstanceOf(JogoException.class).hasMessageContaining("região");
	}

	@Test
	void ladrilhoOcupadoRejeitado() {
		Vila vila = vila("100", "100", "100");
		service.criar(vila, TipoConstrucao.CASA, 1, 0, 0);
		assertThatThrownBy(() -> service.criar(vila, TipoConstrucao.CASA, 1, 0, 0))
				.isInstanceOf(JogoException.class).hasMessageContaining("ocupado");
	}

	@Test
	void recursosInsuficientesNaoDebitaNemCria() {
		Vila vila = vila("100", "5", "100");
		assertThatThrownBy(() -> service.criar(vila, TipoConstrucao.CASA, 1, 0, 0))
				.isInstanceOf(RecursosInsuficientesException.class);
		assertThat(estoqueService.quantidade(vila, Recurso.MADEIRA)).isEqualByComparingTo("100");
		assertThat(construcaoRepository.findByVilaId(vila.getId())).isEmpty();
	}

	@Test
	void regiaoNaoPossuidaRejeitada() {
		Vila vila = vila("100", "100", "100");
		assertThatThrownBy(() -> service.criar(vila, TipoConstrucao.CASA, 5, 0, 0))
				.isInstanceOf(JogoException.class).hasMessageContaining("não possuída");
	}

	@Test
	void foraDaRegiaoRejeitado() {
		Vila vila = vila("100", "100", "100");
		assertThatThrownBy(() -> service.criar(vila, TipoConstrucao.CASA, 1, 10, 0))
				.isInstanceOf(JogoException.class).hasMessageContaining("fora");
		assertThatThrownBy(() -> service.criar(vila, TipoConstrucao.CASA, 1, 0, -1))
				.isInstanceOf(JogoException.class);
	}

	@Test
	void obterDeOutraVilaDevolve403() {
		Vila a = vila("100", "100", "100");
		Vila b = vila("100", "100", "100");
		Construcao c = service.criar(a, TipoConstrucao.CASA, 1, 0, 0);
		assertThat(service.obter(a, c.getId()).getId()).isEqualTo(c.getId());
		assertThatThrownBy(() -> service.obter(b, c.getId())).isInstanceOf(JogoException.class)
				.satisfies(e -> assertThat(((JogoException) e).getStatus().value()).isEqualTo(403));
	}

}
