package com.example.loginbase.jogo.item.catalogo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.loginbase.jogo.cidadao.Profissao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.item.ItemCategoria;
import com.example.loginbase.jogo.item.ItemSubtipo;
import com.example.loginbase.jogo.recurso.Recurso;

class FerramentaCatalogoTest {

	@Test
	void todasAsFerramentasMapeadas() {
		assertThat(FerramentaCatalogo.values()).hasSize(11);
		for (ItemSubtipo s : ItemSubtipo.values()) {
			if (s.getCategoria() == ItemCategoria.FERRAMENTA) {
				assertThat(FerramentaCatalogo.de(s)).isPresent();
				assertThat(FerramentaCatalogo.de(s).get().subtipo()).isEqualTo(s);
			} else {
				assertThat(FerramentaCatalogo.de(s)).isEmpty();
			}
		}
	}

	@Test
	void profissaoEOficina() {
		assertThat(FerramentaCatalogo.MARTELO.profissao()).isEqualTo(Profissao.CONSTRUTOR);
		assertThat(FerramentaCatalogo.MARTELO.oficina()).isEqualTo(TipoConstrucao.FERRARIA);
		assertThat(FerramentaCatalogo.CARRINHO_DE_MAO.profissao()).isEqualTo(Profissao.CARREGADOR);
		assertThat(FerramentaCatalogo.CARRINHO_DE_MAO.oficina()).isEqualTo(TipoConstrucao.CARPINTARIA);
		assertThat(FerramentaCatalogo.MACHADO.profissao()).isEqualTo(Profissao.MADEIREIRO);
		assertThat(FerramentaCatalogo.KIT_DE_COSTURA.profissao()).isEqualTo(Profissao.COSTUREIRO);
		assertThat(FerramentaCatalogo.KIT_DE_COSTURA.oficina()).isEqualTo(TipoConstrucao.ALFAIATARIA);
		assertThat(FerramentaCatalogo.FACA_DE_CACA.profissao()).isEqualTo(Profissao.CACADOR);
		assertThat(FerramentaCatalogo.BALANCA.profissao()).isEqualTo(Profissao.COMERCIANTE);
		assertThat(FerramentaCatalogo.BALANCA.oficina()).isEqualTo(TipoConstrucao.CARPINTARIA);
		for (FerramentaCatalogo f : FerramentaCatalogo.values()) {
			assertThat(f.profissao()).isNotNull().isNotEqualTo(Profissao.GUERREIRO);
			assertThat(f.oficina()).isNotNull();
			assertThat(f.receitaBase()).isNotEmpty();
		}
	}

	@Test
	void receitaMultiplicaPorNivel() {
		assertThat(FerramentaCatalogo.MACHADO.receita(5))
				.containsExactlyInAnyOrderEntriesOf(Map.of(Recurso.FERRO, 10, Recurso.TABUA, 5));
		assertThat(FerramentaCatalogo.CARRINHO_DE_MAO.receita(2))
				.containsExactlyInAnyOrderEntriesOf(Map.of(Recurso.TABUA, 6, Recurso.FERRO, 2));
		assertThat(FerramentaCatalogo.KIT_DE_COSTURA.receita(3))
				.containsExactlyInAnyOrderEntriesOf(Map.of(Recurso.FERRO, 3, Recurso.TECIDO, 3));
	}

	@Test
	void ferroViraAcoAPartirDoNivel6() {
		assertThat(FerramentaCatalogo.MACHADO.receita(6))
				.containsExactlyInAnyOrderEntriesOf(Map.of(Recurso.ACO, 12, Recurso.TABUA, 6));
	}
}
