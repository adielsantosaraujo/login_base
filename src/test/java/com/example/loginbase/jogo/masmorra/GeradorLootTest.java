package com.example.loginbase.jogo.masmorra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoRecurso;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.suporte.AleatorioSequencia;

/**
 * Testes unitários puros (sem Spring) de {@link GeradorLoot}: garantido por
 * nível, quantidade de rolagens, sementes liberadas por nível, nível do item
 * sorteado e determinismo byte a byte a partir de sequências fixas
 * ({@link AleatorioSequencia}).
 */
class GeradorLootTest {

	private final GeradorLoot gerador = new GeradorLoot();

	// --- Garantido por nível (40N comida, 50N madeira, 50N pedra, 20N ferro) ---

	@Test
	void garantidoDoNivel1() {
		// 2 rolagens no nível 1: ambas em faixa de material (d=50), consomem
		// exatamente 1 sorteio cada, sem sementes/itens.
		Loot loot = gerador.gerar(1, new AleatorioSequencia(50, 50));

		assertThat(loot.recursos()).containsEntry(TipoRecurso.COMIDA, 40_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.MADEIRA, 50_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.PEDRA, 50_000L);
		// ferro garantido 20*1*1000 + 2 rolagens de material (30*1*1000 cada)
		assertThat(loot.recursos()).containsEntry(TipoRecurso.FERRO, 20_000L + 2 * 30_000L);
		assertThat(loot.sementes()).containsEntry(Cultivo.MILHO, 0);
		assertThat(loot.itens()).isEmpty();
	}

	@Test
	void garantidoDoNivel2() {
		Loot loot = gerador.gerar(2, new AleatorioSequencia(50, 50));

		assertThat(loot.recursos()).containsEntry(TipoRecurso.COMIDA, 80_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.MADEIRA, 100_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.PEDRA, 100_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.FERRO, 40_000L + 2 * 60_000L);
	}

	@Test
	void garantidoDoNivel3ComTresRolagens() {
		Loot loot = gerador.gerar(3, new AleatorioSequencia(50, 50, 50));

		assertThat(loot.recursos()).containsEntry(TipoRecurso.COMIDA, 120_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.MADEIRA, 150_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.PEDRA, 150_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.FERRO, 60_000L + 3 * 90_000L);
	}

	@Test
	void garantidoDoNivel4ComTresRolagens() {
		Loot loot = gerador.gerar(4, new AleatorioSequencia(50, 50, 50));

		assertThat(loot.recursos()).containsEntry(TipoRecurso.COMIDA, 160_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.MADEIRA, 200_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.PEDRA, 200_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.FERRO, 80_000L + 3 * 120_000L);
	}

	@Test
	void garantidoDoNivel5ComQuatroRolagens() {
		Loot loot = gerador.gerar(5, new AleatorioSequencia(50, 50, 50, 50));

		assertThat(loot.recursos()).containsEntry(TipoRecurso.COMIDA, 200_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.MADEIRA, 250_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.PEDRA, 250_000L);
		assertThat(loot.recursos()).containsEntry(TipoRecurso.FERRO, 100_000L + 4 * 150_000L);
	}

	// --- Quantidade exata de rolagens por nível ---

	@Test
	void quantidadeDeRolagensExataPorNivelNaoConsomeAMaisNemAMenos() {
		// Sequência com exatamente a quantidade de valores esperada por nível
		// (todas em faixa de material, 1 sorteio por rolagem); se o gerador
		// consumisse mais sorteios do que o previsto, a sequência se esgotaria
		// e lançaria IllegalStateException.
		assertThatThrownBy(() -> gerador.gerar(1, new AleatorioSequencia(50))).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> gerador.gerar(3, new AleatorioSequencia(50, 50))).isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> gerador.gerar(5, new AleatorioSequencia(50, 50, 50))).isInstanceOf(IllegalStateException.class);

		assertThat(gerador.gerar(1, new AleatorioSequencia(50, 50))).isNotNull();
		assertThat(gerador.gerar(2, new AleatorioSequencia(50, 50))).isNotNull();
		assertThat(gerador.gerar(3, new AleatorioSequencia(50, 50, 50))).isNotNull();
		assertThat(gerador.gerar(4, new AleatorioSequencia(50, 50, 50))).isNotNull();
		assertThat(gerador.gerar(5, new AleatorioSequencia(50, 50, 50, 50))).isNotNull();
	}

	// --- Sementes por nível (ABOBORA_DOURADA só a partir de N4) ---

	@Test
	void sementeDoNivel1SoPodeSerMilho() {
		// d=10 (semente) em ambas as rolagens; peso liberado é só MILHO (60),
		// então qualquer r em [0,60) escolhe MILHO. Usamos r=59 (limite superior).
		Loot loot = gerador.gerar(1, new AleatorioSequencia(10, 59, 10, 59));

		assertThat(loot.sementes()).containsEntry(Cultivo.MILHO, 2);
		assertThat(loot.sementes()).containsEntry(Cultivo.BATATA, 0);
		assertThat(loot.sementes()).containsEntry(Cultivo.ABOBORA_DOURADA, 0);
	}

	@Test
	void sementeDoNivel2PodeSerBatata() {
		// pesos liberados: MILHO 60 + BATATA 30 = soma 90; r=60 cai na faixa da BATATA.
		Loot loot = gerador.gerar(2, new AleatorioSequencia(10, 60, 50));

		assertThat(loot.sementes()).containsEntry(Cultivo.BATATA, 1);
		assertThat(loot.sementes()).containsEntry(Cultivo.ABOBORA_DOURADA, 0);
	}

	@Test
	void sementeDoNivel4PodeSerAboboraDourada() {
		// pesos liberados: MILHO 60 + BATATA 30 + ABOBORA 10 = soma 100; r=90 cai na faixa da ABOBORA.
		Loot loot = gerador.gerar(4, new AleatorioSequencia(10, 90, 50, 50));

		assertThat(loot.sementes()).containsEntry(Cultivo.ABOBORA_DOURADA, 1);
	}

	// --- Item sorteado: modelo e nível (min(5, N + 0|1)) ---

	@Test
	void itemNivelCasoBase() {
		// d=70 (item), m=0 (ESPADA), bônus=0
		Loot loot = gerador.gerar(1, new AleatorioSequencia(70, 0, 0, 50));

		assertThat(loot.itens()).hasSize(1);
		Item item = loot.itens().get(0);
		assertThat(item.getModelo()).isEqualTo(ModeloItem.ESPADA);
		assertThat(item.getNivel()).isEqualTo(1);
		assertThat(item.getOrigem()).isEqualTo(OrigemItem.MASMORRA);
		assertThat(item.getStatus()).isEqualTo(StatusItem.DISPONIVEL);
	}

	@Test
	void itemNivelComBonus() {
		// nível 3, d=99 (item), m=4 (ARMADURA_FERRO), bônus=1 -> nível 4
		Loot loot = gerador.gerar(3, new AleatorioSequencia(99, 4, 1, 50, 50));

		Item item = loot.itens().get(0);
		assertThat(item.getModelo()).isEqualTo(ModeloItem.ARMADURA_FERRO);
		assertThat(item.getNivel()).isEqualTo(4);
	}

	@Test
	void itemNivelComCapEm5() {
		// nível 5, d=60 (item), m=4 (ARMADURA_FERRO), bônus=1 -> min(5, 5+1) = 5
		Loot loot = gerador.gerar(5, new AleatorioSequencia(60, 4, 1, 50, 50, 50));

		Item item = loot.itens().get(0);
		assertThat(item.getNivel()).isEqualTo(5);
	}

	// --- Determinismo byte a byte (exemplo do enunciado da task) ---

	@Test
	void sequenciaDoEnunciadoGeraUmaSementeMilhoEUmaEspadaDeNivelMaisUm() {
		// [10, 0, 70, 0, 1] no nível 1 (2 rolagens):
		// rolagem 1: d=10 -> semente; r=0 -> MILHO
		// rolagem 2: d=70 -> item; m=0 -> ESPADA; bônus=1 -> nível 1+1=2
		Loot loot = gerador.gerar(1, new AleatorioSequencia(10, 0, 70, 0, 1));

		assertThat(loot.recursos()).isEqualTo(Map.of(
				TipoRecurso.COMIDA, 40_000L,
				TipoRecurso.MADEIRA, 50_000L,
				TipoRecurso.PEDRA, 50_000L,
				TipoRecurso.FERRO, 20_000L));
		assertThat(loot.sementes()).isEqualTo(Map.of(
				Cultivo.MILHO, 1,
				Cultivo.BATATA, 0,
				Cultivo.ABOBORA_DOURADA, 0));
		assertThat(loot.itens()).hasSize(1);
		Item item = loot.itens().get(0);
		assertThat(item.getModelo()).isEqualTo(ModeloItem.ESPADA);
		assertThat(item.getNivel()).isEqualTo(2);
		assertThat(item.getOrigem()).isEqualTo(OrigemItem.MASMORRA);
		assertThat(item.getStatus()).isEqualTo(StatusItem.DISPONIVEL);
	}

	@Test
	void segundoExemploDoEnunciadoNivel3ComTresRolagens() {
		// nível 3 (3 rolagens), gerador retorna [10, 50, 75, ...]:
		// rolagem 1: d=10 -> semente (única liberada relevante: escolhe pelo próximo valor)
		// rolagem 2: d=50 -> material
		// rolagem 3: d=75 -> item
		Loot loot = gerador.gerar(3, new AleatorioSequencia(10, 0, 50, 75, 0, 0));

		assertThat(loot.sementes().get(Cultivo.MILHO)).isEqualTo(1);
		assertThat(loot.recursos().get(TipoRecurso.FERRO)).isEqualTo(60_000L + 90_000L);
		assertThat(loot.itens()).hasSize(1);
		assertThat(loot.itens().get(0).getModelo()).isEqualTo(ModeloItem.ESPADA);
	}

	// --- Pureza: não muta entrada, resultado imutável ---

	@Test
	void resultadoEImutavel() {
		Loot loot = gerador.gerar(1, new AleatorioSequencia(50, 50));

		assertThatThrownBy(() -> loot.recursos().put(TipoRecurso.FERRO, 0L))
				.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> loot.sementes().put(Cultivo.MILHO, 0))
				.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> loot.itens().add(null)).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void mesmoGeradorProduzResultadosIndependentesEmChamadasSucessivas() {
		Loot primeiro = gerador.gerar(2, new AleatorioSequencia(50, 50));
		Loot segundo = gerador.gerar(2, new AleatorioSequencia(50, 50));

		assertThat(primeiro.recursos()).isEqualTo(segundo.recursos());
		assertThat(primeiro.sementes()).isEqualTo(segundo.sementes());
	}

	// --- Validação de nível de masmorra ---

	@Test
	void nivelDeMasmorraForaDaFaixaLancaExcecao() {
		AleatorioSequencia aleatorio = new AleatorioSequencia(0);
		assertThatThrownBy(() -> gerador.gerar(0, aleatorio)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> gerador.gerar(6, aleatorio)).isInstanceOf(IllegalArgumentException.class);
	}

}
