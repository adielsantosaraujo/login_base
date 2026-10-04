#!/usr/bin/env python3
"""Build de producao dos frontends, integrado ao backend.

Equivalente Python ao alvo `build_front` do Makefile (`make build_front`):
descobre os apps em `frontend/public/*` e `frontend/seguro/*`, executa o build
de cada um via Docker Compose, valida o `dist` gerado, copia os assets para
`static/<nome>/` e o `index.html` para o template do app. O log da execucao
vai para `build.log`.

Uso:
    python3 scripts/build_front.py                    # builda todos os apps
    python3 scripts/build_front.py cadastro_usuario   # builda so os apps citados

Sem argumentos faz a limpeza completa (`limpar_front.limpar()`). Com
argumentos, limpa apenas os artefatos dos apps selecionados, preservando os
dos demais.
"""

import os
import shutil
import subprocess
import sys
from datetime import datetime
from pathlib import Path

from apps_front import App, ErroApps, descobrir_apps
from cores import BLUE_COLOR, CYAN_COLOR, RED_COLOR, RESET_COLOR
from limpar_front import limpar as limpar_tudo

BUILD_LOG = Path("build.log")


def log(msg: str, color: str = "") -> None:
    """Exibe `msg` no terminal (colorida) e grava (sem cor) em build.log."""
    print(f"{color}{msg}{RESET_COLOR}" if color else msg)
    with BUILD_LOG.open("a", encoding="utf-8") as arquivo:
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        arquivo.write(f"[{timestamp}] {msg}\n")


def run(cmd: list[str], env: dict[str, str] | None = None) -> int:
    """Executa `cmd` exibindo a saida no terminal e retorna o codigo de saida."""
    log(f"==> Executando: {' '.join(cmd)}", BLUE_COLOR)
    resultado = subprocess.run(cmd, env=env)
    return resultado.returncode


def selecionar_apps(todos: list[App], nomes: list[str]) -> list[App]:
    """Filtra `todos` pelos `nomes` informados; nome desconhecido gera ErroApps."""
    if not nomes:
        return todos
    por_nome = {app.nome: app for app in todos}
    desconhecidos = [n for n in nomes if n not in por_nome]
    if desconhecidos:
        raise ErroApps(
            f"App(s) desconhecido(s): {', '.join(desconhecidos)}. "
            f"Disponiveis: {', '.join(sorted(por_nome))}."
        )
    return [por_nome[n] for n in dict.fromkeys(nomes)]


def limpar_apps(apps: list[App]) -> bool:
    """Remove apenas dist, static e template dos `apps` informados."""
    try:
        for app in apps:
            for caminho in (app.dist, app.static_dir, app.template.parent):
                if not caminho.exists() and not caminho.is_symlink():
                    continue
                log(f"==> Removendo {caminho}", BLUE_COLOR)
                if caminho.is_dir() and not caminho.is_symlink():
                    shutil.rmtree(caminho)
                else:
                    caminho.unlink()
    except OSError as erro:
        log(f"Erro ao limpar arquivos anteriores: {erro}", RED_COLOR)
        return False

    return True


def gerar_dist(app: App) -> int:
    """Executa o build do `app` em container e valida o dist gerado.

    Usa `docker compose run --rm --build` em vez de `up`: o `--rm` descarta
    o container (e o volume anonimo de node_modules) ao final, garantindo
    um node_modules novo a cada build, e propaga corretamente o codigo de
    saida do `npm run build`. Com `up`, uma falha no build sairia com
    status 0 e o dist antigo (ou inexistente) seria copiado sem aviso.
    `--build` forca o rebuild da imagem se o Dockerfile tiver mudado.
    """
    env = {**os.environ, "FRONT_APP": f"{app.area}/{app.nome}", "FRONT_APP_NOME": app.nome}
    rc = run(["docker", "compose", "run", "--rm", "--build", "frontend-build"], env=env)
    if rc != 0:
        return rc

    index = app.dist / "index.html"
    if not index.is_file() or index.stat().st_size == 0:
        log(f"Erro: {index} nao foi gerado (ou esta vazio).", RED_COLOR)
        return 1

    return 0


def copiar_dist(app: App) -> bool:
    """Copia os assets do build de `app` para o backend.

    Copia tudo de `dist/` exceto `index.html` para `static/<nome>/` e o
    `index.html` para o template do app (servido pelo `PaginaController`).
    """
    try:
        app.static_dir.mkdir(parents=True, exist_ok=True)

        for origem in app.dist.iterdir():
            if origem.name == "index.html":
                continue
            destino = app.static_dir / origem.name
            if origem.is_dir():
                shutil.copytree(origem, destino, dirs_exist_ok=True)
            else:
                shutil.copy2(origem, destino)

        app.template.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(app.dist / "index.html", app.template)
    except OSError as erro:
        log(f"Erro ao copiar arquivos: {erro}", RED_COLOR)
        return False

    return True


def main() -> None:
    if BUILD_LOG.exists():
        BUILD_LOG.unlink()

    log("==> Iniciando build do frontend", BLUE_COLOR)

    nomes = sys.argv[1:]
    try:
        todos = descobrir_apps()
        if not todos:
            raise ErroApps("Nenhum app encontrado em frontend/public ou frontend/seguro.")
        apps = selecionar_apps(todos, nomes)
    except ErroApps as erro:
        log(f"Erro: {erro}", RED_COLOR)
        sys.exit(1)

    limpou = limpar_apps(apps) if nomes else limpar_tudo(apps)
    if not limpou:
        sys.exit(1)

    for app in apps:
        log(f"==> App {app.area}/{app.nome}", BLUE_COLOR)

        rc = gerar_dist(app)
        if rc != 0:
            log(f"Erro no build do app {app.nome} (codigo {rc}).", RED_COLOR)
            sys.exit(rc)

        if not copiar_dist(app):
            log(f"Erro ao copiar o build de {app.nome} para o backend.", RED_COLOR)
            sys.exit(1)

    log(f"Build concluido com sucesso: {', '.join(a.nome for a in apps)}.", CYAN_COLOR)
    sys.exit(0)


if __name__ == "__main__":
    main()
