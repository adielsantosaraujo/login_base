#!/usr/bin/env python3
"""Build de producao do frontend, integrado ao backend.

Equivalente Python ao alvo `build_front` do Makefile (`make build_front`):
executa o build do frontend via Docker Compose, valida a geracao do
`frontend/dist`, copia os assets para o backend e registra o log da
execucao em `build.log`.

A cada execucao, antes do build, remove `frontend/dist`,
`static/app/` e o template legado `sistema/seguro/index.html`.
"""

import shutil
import subprocess
import sys
from datetime import datetime
from pathlib import Path

from cores import BLUE_COLOR, CYAN_COLOR, RED_COLOR, RESET_COLOR

DIST_DIR = Path("frontend/dist")
INDEX_FILE = DIST_DIR / "index.html"
STATIC_APP_DIR = Path("src/main/resources/static/app")
TEMPLATE_FILE = Path("src/main/resources/templates/sistema/seguro/app/index.html")
LEGACY_TEMPLATE_FILE = Path("src/main/resources/templates/sistema/seguro/index.html")
BUILD_LOG = Path("build.log")


def log(msg: str, color: str = "") -> None:
    """Exibe `msg` no terminal (colorida) e grava (sem cor) em build.log."""
    print(f"{color}{msg}{RESET_COLOR}" if color else msg)
    with BUILD_LOG.open("a", encoding="utf-8") as arquivo:
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        arquivo.write(f"[{timestamp}] {msg}\n")


def run(cmd: list[str]) -> int:
    """Executa `cmd` exibindo a saida no terminal e retorna o codigo de saida."""
    log(f"==> Executando: {' '.join(cmd)}", BLUE_COLOR)
    resultado = subprocess.run(cmd)
    return resultado.returncode


def limpar() -> bool:
    """Remove dist, static/app e o template legado de builds anteriores."""
    try:
        for caminho in (DIST_DIR, STATIC_APP_DIR, LEGACY_TEMPLATE_FILE):
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


def gerar_dist() -> int:
    """Executa o build do frontend em container e valida o dist gerado.

    Usa `docker compose run --rm --build` em vez de `up`: o `--rm` descarta
    o container (e o volume anonimo de node_modules) ao final, garantindo
    um node_modules novo a cada build, e propaga corretamente o codigo de
    saida do `npm run build`. Com `up`, uma falha no build sairia com
    status 0 e o dist antigo (ou inexistente) seria copiado sem aviso.
    `--build` forca o rebuild da imagem se o Dockerfile tiver mudado.
    """
    rc = run(["docker", "compose", "run", "--rm", "--build", "frontend-build"])
    if rc != 0:
        return rc

    if not INDEX_FILE.is_file() or INDEX_FILE.stat().st_size == 0:
        log(f"Erro: {INDEX_FILE} nao foi gerado (ou esta vazio).", RED_COLOR)
        return 1

    return 0


def copiar_dist() -> bool:
    """Copia os assets do build para o backend.

    Cria `static/app/` (ja limpo por `limpar()`), copia tudo de
    `frontend/dist/` exceto `index.html` para `static/app/` e copia o `index.html` para o template servido pelo `PaginaController`.
    """
    try:
        STATIC_APP_DIR.mkdir(parents=True, exist_ok=True)

        for origem in DIST_DIR.iterdir():
            if origem.name == "index.html":
                continue
            destino = STATIC_APP_DIR / origem.name
            if origem.is_dir():
                shutil.copytree(origem, destino)
            else:
                shutil.copy2(origem, destino)

        TEMPLATE_FILE.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(INDEX_FILE, TEMPLATE_FILE)
    except OSError as erro:
        log(f"Erro ao copiar arquivos: {erro}", RED_COLOR)
        return False

    return True


def main() -> None:
    if BUILD_LOG.exists():
        BUILD_LOG.unlink()

    log("==> Iniciando build do frontend", BLUE_COLOR)

    if not limpar():
        sys.exit(1)

    rc = gerar_dist()
    if rc != 0:
        log(f"Erro no build do frontend (codigo {rc}).", RED_COLOR)
        sys.exit(rc)

    if not copiar_dist():
        log("Erro ao copiar o build para o backend.", RED_COLOR)
        sys.exit(1)

    log("Build concluido com sucesso.", CYAN_COLOR)
    sys.exit(0)


if __name__ == "__main__":
    main()
