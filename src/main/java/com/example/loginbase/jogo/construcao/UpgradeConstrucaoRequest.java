package com.example.loginbase.jogo.construcao;

/** Pedido de upgrade: novoNivel opcional (se informado deve ser o próximo); novaX/novaY opcionais (padrão: posição atual). */
public record UpgradeConstrucaoRequest(NivelConstrucao novoNivel, Integer novaX, Integer novaY) {
}
