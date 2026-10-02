package com.example.loginbase.jogo.pedra;

import com.example.loginbase.jogo.item.CodigoBonus;
import com.example.loginbase.jogo.item.FaixaBonus;

public record BonusPedra(CodigoBonus codigo, FaixaBonus magnitude, int valor) {
}
