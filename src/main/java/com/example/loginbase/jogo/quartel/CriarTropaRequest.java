package com.example.loginbase.jogo.quartel;

import java.util.List;

public record CriarTropaRequest(String nome, List<MembroTropaRequest> membros) {
}
