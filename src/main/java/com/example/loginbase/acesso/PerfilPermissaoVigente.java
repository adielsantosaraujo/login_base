package com.example.loginbase.acesso;

/**
 * Projeção de um perfil vigente de um usuário e uma de suas permissões
 * (uma linha por combinação perfil/permissão). {@code permissao} é nula
 * quando o perfil vigente não tem nenhuma permissão associada.
 */
public record PerfilPermissaoVigente(String perfil, String permissao) {
}
