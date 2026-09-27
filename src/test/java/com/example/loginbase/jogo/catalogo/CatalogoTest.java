package com.example.loginbase.jogo.catalogo;

import static com.example.loginbase.jogo.catalogo.TipoRecurso.COMIDA;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.FERRO;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.MADEIRA;
import static com.example.loginbase.jogo.catalogo.TipoRecurso.PEDRA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Testes unitários puros (sem Spring) do catálogo estático do jogo: validam
 * fórmulas e as tabelas A.1, A.3, A.5–A.9 do design contra valores esperados.
 */
class CatalogoTest {

	@Test
	void capacidadeDoArmazemDobraPorNivelTabelaA1() {
		assertThat(TipoRecurso.capacidadeArmazem(1)).isEqualTo(500);
		assertThat(TipoRecurso.capacidadeArmazem(2)).isEqualTo(1000);
		assertThat(TipoRecurso.capacidadeArmazem(3)).isEqualTo(2000);
		assertThat(TipoRecurso.capacidadeArmazem(4)).isEqualTo(4000);
		assertThat(TipoRecurso.capacidadeArmazem(5)).isEqualTo(8000);
	}

	@Test
	void custoETempoDosPrediosSeguemTabelaA3() {
		assertThat(TipoPredio.CENTRO_VILA.custo(1)).isEqualTo(Custo.de(MADEIRA, 150, PEDRA, 150));
		assertThat(TipoPredio.CENTRO_VILA.tempoSegundos(1)).isEqualTo(120);
		assertThat(TipoPredio.CENTRO_VILA.custo(2)).isEqualTo(Custo.de(MADEIRA, 225, PEDRA, 225));
		assertThat(TipoPredio.CENTRO_VILA.tempoSegundos(2)).isEqualTo(240);
		assertThat(TipoPredio.CENTRO_VILA.custo(3)).isEqualTo(Custo.de(MADEIRA, 338, PEDRA, 338));
		assertThat(TipoPredio.CENTRO_VILA.tempoSegundos(3)).isEqualTo(480);
		assertThat(TipoPredio.CENTRO_VILA.custo(4)).isEqualTo(Custo.de(MADEIRA, 506, PEDRA, 506));
		assertThat(TipoPredio.CENTRO_VILA.tempoSegundos(4)).isEqualTo(960);
		assertThat(TipoPredio.CENTRO_VILA.custo(5)).isEqualTo(Custo.de(MADEIRA, 759, PEDRA, 759));
		assertThat(TipoPredio.CENTRO_VILA.tempoSegundos(5)).isEqualTo(1920);

		assertThat(TipoPredio.ARMAZEM.custo(1)).isEqualTo(Custo.de(MADEIRA, 100, PEDRA, 60));
		assertThat(TipoPredio.ARMAZEM.custo(4)).isEqualTo(Custo.de(MADEIRA, 338, PEDRA, 203));
		assertThat(TipoPredio.ARMAZEM.custo(5)).isEqualTo(Custo.de(MADEIRA, 506, PEDRA, 304));

		assertThat(TipoPredio.FAZENDA.custo(3)).isEqualTo(Custo.de(MADEIRA, 180, PEDRA, 90));

		assertThat(TipoPredio.SERRARIA.custo(4)).isEqualTo(Custo.de(MADEIRA, 203, PEDRA, 135));

		assertThat(TipoPredio.PEDREIRA.custo(5)).isEqualTo(Custo.de(MADEIRA, 405, PEDRA, 101));

		assertThat(TipoPredio.MINA_FERRO.custo(2)).isEqualTo(Custo.de(MADEIRA, 150, PEDRA, 120));
		assertThat(TipoPredio.MINA_FERRO.tempoSegundos(2)).isEqualTo(180);

		assertThat(TipoPredio.FORJA.custo(1)).isEqualTo(Custo.de(MADEIRA, 120, PEDRA, 100, FERRO, 40));
		assertThat(TipoPredio.FORJA.custo(5)).isEqualTo(Custo.de(MADEIRA, 608, PEDRA, 506, FERRO, 203));
		assertThat(TipoPredio.FORJA.tempoSegundos(5)).isEqualTo(1920);

		assertThat(TipoPredio.QUARTEL.custo(1)).isEqualTo(Custo.de(MADEIRA, 150, PEDRA, 120, FERRO, 40));
		assertThat(TipoPredio.QUARTEL.custo(5)).isEqualTo(Custo.de(MADEIRA, 759, PEDRA, 608, FERRO, 203));
	}

	@Test
	void efeitosDosPrediosPorNivel() {
		assertThat(TipoPredio.ARMAZEM.capacidadeRecurso(3)).isEqualTo(2000);
		assertThat(TipoPredio.FAZENDA.numeroCanteiros(4)).isEqualTo(4);
		assertThat(TipoPredio.SERRARIA.producaoAdicionalPorHora(2)).isEqualTo(60);
		assertThat(TipoPredio.PEDREIRA.producaoAdicionalPorHora(2)).isEqualTo(40);
		assertThat(TipoPredio.MINA_FERRO.producaoAdicionalPorHora(2)).isEqualTo(20);
		assertThat(TipoPredio.FORJA.nivelMaximoForjavel(3)).isEqualTo(3);
		assertThat(TipoPredio.QUARTEL.capacidadeExercito(2)).isEqualTo(6);
	}

	@Test
	void nivelDePredioForaDoIntervaloRejeitado() {
		assertThatThrownBy(() -> TipoPredio.CENTRO_VILA.custo(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> TipoPredio.CENTRO_VILA.custo(6)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void cultivosSeguemTabelaA5() {
		assertThat(Cultivo.TRIGO.producaoComidaPorHora()).isEqualTo(20);
		assertThat(Cultivo.TRIGO.exigeSemente()).isFalse();
		assertThat(Cultivo.MILHO.producaoComidaPorHora()).isEqualTo(30);
		assertThat(Cultivo.MILHO.exigeSemente()).isTrue();
		assertThat(Cultivo.BATATA.producaoComidaPorHora()).isEqualTo(45);
		assertThat(Cultivo.ABOBORA_DOURADA.producaoComidaPorHora()).isEqualTo(70);
		assertThat(Cultivo.ABOBORA_DOURADA.exigeSemente()).isTrue();
	}

	@Test
	void atributosDosItensSeguemTabelaA6() {
		assertThat(ModeloItem.ESPADA.atributos(1)).isEqualTo(new ModeloItem.AtributosItem(6, 0, 1));
		assertThat(ModeloItem.ESPADA.atributos(2)).isEqualTo(new ModeloItem.AtributosItem(8, 0, 1));
		assertThat(ModeloItem.ESPADA.atributos(3)).isEqualTo(new ModeloItem.AtributosItem(10, 0, 1));

		assertThat(ModeloItem.LANCA.atributos(1)).isEqualTo(new ModeloItem.AtributosItem(5, 0, 1));
		assertThat(ModeloItem.ARCO.atributos(1)).isEqualTo(new ModeloItem.AtributosItem(4, 0, 3));
		assertThat(ModeloItem.ARMADURA_COURO.atributos(1)).isEqualTo(new ModeloItem.AtributosItem(0, 2, 0));
		assertThat(ModeloItem.ARMADURA_FERRO.atributos(2)).isEqualTo(new ModeloItem.AtributosItem(0, 5, 0));

		assertThat(ModeloItem.ESPADA.categoria()).isEqualTo(CategoriaItem.ARMA);
		assertThat(ModeloItem.ARMADURA_COURO.categoria()).isEqualTo(CategoriaItem.ARMADURA);
	}

	@Test
	void receitasDeForjaSeguemTabelaA7() {
		assertThat(ModeloItem.ESPADA.custoTotal(1, 1)).isEqualTo(Custo.de(MADEIRA, 20, FERRO, 30));
		assertThat(ModeloItem.ESPADA.tempoTotalSegundos(1, 1, 1)).isEqualTo(60);
		assertThat(ModeloItem.ESPADA.custoTotal(2, 3)).isEqualTo(Custo.de(MADEIRA, 120, FERRO, 180));
		assertThat(ModeloItem.ESPADA.tempoTotalSegundos(2, 3, 1)).isEqualTo(360);

		assertThat(ModeloItem.LANCA.custoTotal(1, 1)).isEqualTo(Custo.de(MADEIRA, 40, FERRO, 20));
		assertThat(ModeloItem.ARCO.custoTotal(1, 1)).isEqualTo(Custo.de(MADEIRA, 50, FERRO, 5));
		assertThat(ModeloItem.ARMADURA_COURO.custoTotal(1, 1)).isEqualTo(Custo.de(COMIDA, 20, MADEIRA, 10, FERRO, 5));
		assertThat(ModeloItem.ARMADURA_COURO.tempoTotalSegundos(1, 1, 1)).isEqualTo(45);
		assertThat(ModeloItem.ARMADURA_FERRO.custoTotal(1, 1)).isEqualTo(Custo.de(MADEIRA, 10, FERRO, 40));
		assertThat(ModeloItem.ARMADURA_FERRO.tempoTotalSegundos(1, 1, 1)).isEqualTo(90);

		assertThat(ModeloItem.ESPADA.tempoTotalSegundos(1, 3, 2)).isEqualTo(90);
	}

	@Test
	void tropasSeguemTabelaA8() {
		assertThat(TipoTropa.SOLDADO.armaExigida()).isEqualTo(ModeloItem.ESPADA);
		assertThat(TipoTropa.SOLDADO.hp()).isEqualTo(30);
		assertThat(TipoTropa.SOLDADO.defesaBase()).isEqualTo(1);
		assertThat(TipoTropa.SOLDADO.movimento()).isEqualTo(3);
		assertThat(TipoTropa.SOLDADO.comida()).isEqualTo(50);
		assertThat(TipoTropa.SOLDADO.tempoTreinoSegundos()).isEqualTo(60);
		assertThat(TipoTropa.SOLDADO.nivelMinimoQuartel()).isEqualTo(1);

		assertThat(TipoTropa.ARQUEIRO.armaExigida()).isEqualTo(ModeloItem.ARCO);
		assertThat(TipoTropa.ARQUEIRO.hp()).isEqualTo(22);
		assertThat(TipoTropa.ARQUEIRO.defesaBase()).isEqualTo(0);
		assertThat(TipoTropa.ARQUEIRO.movimento()).isEqualTo(3);
		assertThat(TipoTropa.ARQUEIRO.nivelMinimoQuartel()).isEqualTo(2);

		assertThat(TipoTropa.LANCEIRO.armaExigida()).isEqualTo(ModeloItem.LANCA);
		assertThat(TipoTropa.LANCEIRO.hp()).isEqualTo(40);
		assertThat(TipoTropa.LANCEIRO.defesaBase()).isEqualTo(2);
		assertThat(TipoTropa.LANCEIRO.movimento()).isEqualTo(2);
		assertThat(TipoTropa.LANCEIRO.comida()).isEqualTo(60);
		assertThat(TipoTropa.LANCEIRO.tempoTreinoSegundos()).isEqualTo(75);
		assertThat(TipoTropa.LANCEIRO.nivelMinimoQuartel()).isEqualTo(3);
	}

	@Test
	void inimigosSeguemTabelaA9() {
		assertThat(TipoInimigo.GOBLIN.hp()).isEqualTo(15);
		assertThat(TipoInimigo.GOBLIN.ataque()).isEqualTo(6);
		assertThat(TipoInimigo.GOBLIN.defesa()).isEqualTo(1);
		assertThat(TipoInimigo.GOBLIN.alcance()).isEqualTo(1);
		assertThat(TipoInimigo.GOBLIN.movimento()).isEqualTo(3);

		assertThat(TipoInimigo.ESQUELETO_ARQUEIRO.hp()).isEqualTo(12);
		assertThat(TipoInimigo.ESQUELETO_ARQUEIRO.alcance()).isEqualTo(3);
		assertThat(TipoInimigo.ESQUELETO_ARQUEIRO.defesa()).isEqualTo(0);

		assertThat(TipoInimigo.ORC.hp()).isEqualTo(30);
		assertThat(TipoInimigo.ORC.ataque()).isEqualTo(9);
		assertThat(TipoInimigo.ORC.defesa()).isEqualTo(3);

		assertThat(TipoInimigo.TROLL.hp()).isEqualTo(70);
		assertThat(TipoInimigo.TROLL.ataque()).isEqualTo(13);
		assertThat(TipoInimigo.TROLL.defesa()).isEqualTo(5);
		assertThat(TipoInimigo.TROLL.movimento()).isEqualTo(2);
	}

	@Test
	void mapaTaticoTemObstaculosSpawnsEPosicoesJogador() {
		MapaMasmorra mapa = MapaMasmorra.PADRAO;

		assertThat(mapa.largura()).isEqualTo(8);
		assertThat(mapa.altura()).isEqualTo(8);
		assertThat(mapa.obstaculos()).hasSize(8);
		assertThat(mapa.spawnsInimigos()).hasSize(5);
		assertThat(mapa.posicoesJogador()).hasSize(4);

		assertThat(mapa.spawnsInimigos()).containsEntry("S1", new MapaMasmorra.Posicao(3, 0));
		assertThat(mapa.obstaculos()).contains(new MapaMasmorra.Posicao(3, 2), new MapaMasmorra.Posicao(4, 5));
		assertThat(mapa.posicoesJogador()).contains(new MapaMasmorra.Posicao(2, 7),
				new MapaMasmorra.Posicao(5, 7));
	}

	@Test
	void composicoesDeMasmorraSeguemTabelaA9() {
		assertThat(CatalogoMasmorras.porNivel(1).composicaoInimigos())
				.containsExactly(TipoInimigo.GOBLIN, TipoInimigo.GOBLIN, TipoInimigo.GOBLIN);

		assertThat(CatalogoMasmorras.porNivel(2).composicaoInimigos())
				.containsExactly(TipoInimigo.ESQUELETO_ARQUEIRO, TipoInimigo.GOBLIN, TipoInimigo.GOBLIN,
						TipoInimigo.GOBLIN);

		assertThat(CatalogoMasmorras.porNivel(3).composicaoInimigos())
				.containsExactly(TipoInimigo.ORC, TipoInimigo.GOBLIN, TipoInimigo.GOBLIN,
						TipoInimigo.ESQUELETO_ARQUEIRO, TipoInimigo.ESQUELETO_ARQUEIRO);

		assertThat(CatalogoMasmorras.porNivel(4).composicaoInimigos())
				.containsExactly(TipoInimigo.ORC, TipoInimigo.ORC, TipoInimigo.ORC,
						TipoInimigo.ESQUELETO_ARQUEIRO, TipoInimigo.ESQUELETO_ARQUEIRO);

		assertThat(CatalogoMasmorras.porNivel(5).composicaoInimigos())
				.containsExactly(TipoInimigo.TROLL, TipoInimigo.ORC, TipoInimigo.ORC,
						TipoInimigo.ESQUELETO_ARQUEIRO, TipoInimigo.ESQUELETO_ARQUEIRO);

		assertThat(CatalogoMasmorras.porNivel(5).mapa()).isSameAs(MapaMasmorra.PADRAO);
		assertThatThrownBy(() -> CatalogoMasmorras.porNivel(6)).isInstanceOf(IllegalArgumentException.class);
	}

}
