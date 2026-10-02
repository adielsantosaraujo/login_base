package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.loginbase.jogo.batalha.Combatente;
import com.example.loginbase.jogo.batalha.LadoCombate;
import com.example.loginbase.jogo.batalha.TipoInimigo;
import java.util.List;
import org.junit.jupiter.api.Test;

class GeradorInimigosTest {

	private final GeradorInimigos gerador = new GeradorInimigos();

	@Test
	void multiplicadorN5() {
		assertThat(TipoInimigo.multiplicador(5)).isEqualTo(1.6);
	}

	@Test
	void senhorOrcN5Tem352Pv() {
		assertThat(TipoInimigo.SENHOR_ORC.atributos(5).pvMax()).isEqualTo(352);
	}

	@Test
	void n5Tem7ComunsMaisChefeNoFim() {
		List<Combatente> l = gerador.gerar(5, 42L);
		assertThat(l).hasSize(8);
		assertThat(l.get(7).nome()).isEqualTo("Senhor orc");
		assertThat(l.get(7).pvMax()).isEqualTo(352);
		List<String> permitidos = GrupoInimigos.comuns(5).stream().map(TipoInimigo::nome).toList();
		assertThat(l.subList(0, 7)).allMatch(c -> permitidos.contains(c.nome()));
		for (int i = 0; i < l.size(); i++) {
			assertThat(l.get(i).id()).isEqualTo(i);
		}
	}

	@Test
	void n2SemChefe() {
		List<Combatente> l = gerador.gerar(2, 1L);
		assertThat(l).hasSize(4);
		assertThat(GrupoInimigos.chefe(2)).isEmpty();
	}

	@Test
	void chefesPorFaixa() {
		assertThat(GrupoInimigos.chefe(3)).contains(TipoInimigo.CHEFE_GOBLIN);
		assertThat(GrupoInimigos.chefe(6)).contains(TipoInimigo.SENHOR_ORC);
		assertThat(GrupoInimigos.chefe(9)).contains(TipoInimigo.TROLL_ANCIAO);
		assertThat(GrupoInimigos.chefe(10)).contains(TipoInimigo.DRAGAO_JOVEM);
		assertThat(gerador.gerar(10, 3L)).hasSize(9);
	}

	@Test
	void mesmaSementeMesmaComposicao() {
		assertThat(gerador.gerar(7, 99L)).isEqualTo(gerador.gerar(7, 99L));
	}

	@Test
	void todosDoLadoInimigo() {
		for (int n = 1; n <= 10; n++) {
			assertThat(gerador.gerar(n, n)).allMatch(c -> c.lado() == LadoCombate.INIMIGO);
		}
	}
}
