package com.example.loginbase.jogo.api;

/**
 * Corpo padrão de erro da API do jogo: {@code codigo} é, na maioria dos
 * casos, o nome de um valor de {@link com.example.loginbase.jogo.CodigoErro}
 * (ex.: {@code RECURSOS_INSUFICIENTES}), mas alguns códigos de erro tratados
 * por {@link ErroApiHandler} (ex.: {@code ERRO_INTERNO}) não correspondem a
 * nenhuma violação de regra de jogo e por isso não existem nesse enum — daí
 * este campo ser {@code String} e não o próprio enum.
 */
public record ErroDto(String codigo, String mensagem) {
}
