package com.example.loginbase.jogo.dto;

import java.util.List;

import com.example.loginbase.jogo.modelo.TipoRegiao;

/** Região da prévia do mapa com seus bônus. */
public record RegiaoPreviaDTO(int indice, TipoRegiao tipo, List<RegiaoBonusDTO> bonus) {
}
