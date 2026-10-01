package com.example.loginbase.jogo.construcao;

import com.example.loginbase.jogo.cidadao.Profissao;

public record AlocacaoRequest(Long cidadaoId, Profissao profissao) {
}
