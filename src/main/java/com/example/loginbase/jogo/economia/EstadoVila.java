package com.example.loginbase.jogo.economia;

import java.util.List;

import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.EstoqueSemente;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.Vila;

/**
 * Snapshot imutável do estado completo de uma vila (já sincronizado por
 * {@link VilaService#consultar(long)}), usado por mapeadores da camada de API
 * para montar os DTOs de resposta. Não substitui as entidades: apenas agrupa
 * o que hoje é lido em consultas separadas por {@code vilaId}.
 */
public record EstadoVila(
		Vila vila,
		List<Predio> predios,
		List<Canteiro> canteiros,
		List<EstoqueSemente> sementes,
		List<Item> itens,
		List<Unidade> unidades,
		List<Ordem> ordens) {
}
