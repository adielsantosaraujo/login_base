#!/usr/bin/env python3
"""Mede o consumo da sessão principal do Claude Code (orquestrador) a partir do
transcript JSONL da sessão e imprime, em stdout, duas partes separadas por uma
linha em branco: a linha de tabela Markdown da sessão (sem os arquivos lidos
embutidos na célula) e, em seguida, uma seção "### — Sessão principal
(orquestrador)" com a lista de arquivos lidos dividida em "Harness" e
"Negócio". Ambas as partes são prontas para colar no relatório
resumo_utilizacao_agentes.md.

Uso:
    python3 consumo_sessao.py [--sessao <session-id>] [--projeto <caminho>]

Somente biblioteca padrão do Python 3.
"""

import argparse
import glob
import json
import os
import sys
from datetime import datetime


def dir_projeto_para_slug(caminho_projeto):
    """Converte o caminho absoluto do projeto no nome de diretório usado em
    ~/.claude/projects: todo caractere não alfanumérico vira '-'."""
    abs_caminho = os.path.abspath(caminho_projeto)
    return "".join(c if c.isalnum() else "-" for c in abs_caminho)


def encontrar_transcript(sessao, projeto):
    base = os.path.expanduser("~/.claude/projects")

    if sessao:
        padrao = os.path.join(base, "*", f"{sessao}.jsonl")
        candidatos = glob.glob(padrao)
        if not candidatos:
            print(
                f"Erro: nenhum transcript encontrado para a sessão '{sessao}' "
                f"(padrão procurado: {padrao})",
                file=sys.stderr,
            )
            sys.exit(1)
        candidatos.sort(key=os.path.getmtime, reverse=True)
        return candidatos[0]

    projeto_abs = os.path.abspath(projeto)
    slug = dir_projeto_para_slug(projeto_abs)
    dir_transcripts = os.path.join(base, slug)
    if not os.path.isdir(dir_transcripts):
        print(
            f"Erro: diretório de transcripts não encontrado para o projeto "
            f"'{projeto_abs}' (esperado em {dir_transcripts})",
            file=sys.stderr,
        )
        sys.exit(1)

    candidatos = glob.glob(os.path.join(dir_transcripts, "*.jsonl"))
    if not candidatos:
        print(
            f"Erro: nenhum arquivo .jsonl encontrado em {dir_transcripts}",
            file=sys.stderr,
        )
        sys.exit(1)
    candidatos.sort(key=os.path.getmtime, reverse=True)
    return candidatos[0]


def carregar_eventos(caminho_transcript):
    eventos = []
    with open(caminho_transcript, "r", encoding="utf-8") as f:
        for linha in f:
            linha = linha.strip()
            if not linha:
                continue
            try:
                eventos.append(json.loads(linha))
            except json.JSONDecodeError:
                continue
    return eventos


def eh_harness(caminho_bruto):
    if caminho_bruto.startswith("skill:"):
        return True
    normalizado = caminho_bruto.replace("\\", "/")
    partes = normalizado.split("/")
    if ".claude" in partes:
        return True
    if normalizado.endswith("CLAUDE.md"):
        return True
    return False


def formatar_caminho_exibicao(caminho_bruto, projeto_abs, home_abs):
    if caminho_bruto.startswith("skill:"):
        return caminho_bruto

    try:
        abs_path = os.path.abspath(os.path.expanduser(caminho_bruto))
    except Exception:
        return caminho_bruto

    if abs_path == projeto_abs or abs_path.startswith(projeto_abs + os.sep):
        return os.path.relpath(abs_path, projeto_abs)

    if abs_path == home_abs or abs_path.startswith(home_abs + os.sep):
        return "~" + abs_path[len(home_abs):]

    return abs_path


def formatar_duracao(inicio_iso, fim_iso):
    def parse(ts):
        return datetime.fromisoformat(ts.replace("Z", "+00:00"))

    inicio = parse(inicio_iso)
    fim = parse(fim_iso)
    total_seg = round((fim - inicio).total_seconds())
    if total_seg < 0:
        total_seg = 0

    horas, resto = divmod(total_seg, 3600)
    minutos, segundos = divmod(resto, 60)

    if horas >= 1:
        return f"{horas}h {minutos:02d}m"
    return f"{minutos}m {segundos:02d}s"


def abreviar_modelo(nome_modelo):
    if not nome_modelo:
        return "?"
    if nome_modelo.startswith("claude-opus-"):
        return "opus"
    if nome_modelo.startswith("claude-sonnet-"):
        return "sonnet"
    if nome_modelo.startswith("claude-haiku-"):
        return "haiku"
    return nome_modelo


def formatar_numero_ptbr(n):
    return f"{n:,}".replace(",", ".")


def processar(eventos):
    primeiro_ts = None
    ultimo_ts = None

    usage_por_id = {}
    ultimo_mid = None
    ultimo_modelo = None

    tool_use_ids_vistos = set()
    total_tool_uses = 0

    arquivos_ordem = []  # lista de caminhos brutos, na ordem de primeira ocorrência
    arquivos_vistos = set()

    for ev in eventos:
        if ev.get("isSidechain"):
            continue

        ts = ev.get("timestamp")
        if ts:
            if primeiro_ts is None:
                primeiro_ts = ts
            ultimo_ts = ts

        if ev.get("type") != "assistant":
            continue

        msg = ev.get("message") or {}
        mid = msg.get("id")
        usage = msg.get("usage")

        if mid:
            ultimo_mid = mid
            ultimo_modelo = msg.get("model")
            if usage is not None and mid not in usage_por_id:
                usage_por_id[mid] = usage

        for bloco in msg.get("content") or []:
            if bloco.get("type") != "tool_use":
                continue
            bid = bloco.get("id")
            if bid in tool_use_ids_vistos:
                continue
            tool_use_ids_vistos.add(bid)
            total_tool_uses += 1

            nome = bloco.get("name")
            entrada = bloco.get("input") or {}

            caminho_bruto = None
            if nome == "Read":
                caminho_bruto = entrada.get("file_path")
            elif nome == "Skill":
                skill = entrada.get("skill")
                if skill:
                    caminho_bruto = f"skill:{skill}"

            if caminho_bruto and caminho_bruto not in arquivos_vistos:
                arquivos_vistos.add(caminho_bruto)
                arquivos_ordem.append(caminho_bruto)

    return {
        "primeiro_ts": primeiro_ts,
        "ultimo_ts": ultimo_ts,
        "usage_por_id": usage_por_id,
        "ultimo_mid": ultimo_mid,
        "ultimo_modelo": ultimo_modelo,
        "total_tool_uses": total_tool_uses,
        "arquivos_ordem": arquivos_ordem,
    }


def main():
    parser = argparse.ArgumentParser(
        description=(
            "Mede o consumo da sessão principal do Claude Code (orquestrador) "
            "a partir do transcript JSONL da sessão."
        )
    )
    parser.add_argument("--sessao", default=None, help="session-id do transcript")
    parser.add_argument(
        "--projeto",
        default=os.getcwd(),
        help="caminho do projeto (padrão: diretório atual)",
    )
    args = parser.parse_args()

    caminho_transcript = encontrar_transcript(args.sessao, args.projeto)
    eventos = carregar_eventos(caminho_transcript)

    if not eventos:
        print(f"Erro: transcript vazio ou ilegível: {caminho_transcript}", file=sys.stderr)
        sys.exit(1)

    dados = processar(eventos)

    if dados["primeiro_ts"] is None or dados["ultimo_ts"] is None:
        print(f"Erro: nenhuma linha com timestamp em {caminho_transcript}", file=sys.stderr)
        sys.exit(1)

    if dados["ultimo_mid"] is None or dados["ultimo_mid"] not in dados["usage_por_id"]:
        print(
            f"Erro: nenhuma mensagem assistant com usage encontrada em {caminho_transcript}",
            file=sys.stderr,
        )
        sys.exit(1)

    usage_final = dados["usage_por_id"][dados["ultimo_mid"]]
    total_tokens = (
        usage_final.get("input_tokens", 0)
        + usage_final.get("cache_creation_input_tokens", 0)
        + usage_final.get("cache_read_input_tokens", 0)
        + usage_final.get("output_tokens", 0)
    )

    duracao = formatar_duracao(dados["primeiro_ts"], dados["ultimo_ts"])
    modelo = abreviar_modelo(dados["ultimo_modelo"])

    projeto_abs = os.path.abspath(args.projeto)
    home_abs = os.path.expanduser("~")

    harness = []
    negocio = []
    for bruto in dados["arquivos_ordem"]:
        exibicao = formatar_caminho_exibicao(bruto, projeto_abs, home_abs)
        if eh_harness(bruto):
            harness.append(exibicao)
        else:
            negocio.append(exibicao)

    tokens_txt = formatar_numero_ptbr(total_tokens)

    linha = (
        f"| — | Sessão principal (orquestrador) | orquestração | {modelo} | "
        f"✅ até o início do relatório | {dados['total_tool_uses']} | {duracao} | {tokens_txt} |"
    )

    def formatar_lista(itens):
        if not itens:
            return "- —"
        return "\n".join(f"- {item}" for item in itens)

    secao = (
        "### — Sessão principal (orquestrador)\n"
        "\n"
        "**Harness**\n"
        f"{formatar_lista(harness)}\n"
        "\n"
        "**Negócio**\n"
        f"{formatar_lista(negocio)}"
    )

    print(linha)
    print()
    print(secao)


if __name__ == "__main__":
    main()
