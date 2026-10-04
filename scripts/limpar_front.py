#!/usr/bin/env python3
"""Limpeza dos artefatos de build do frontend.

Remove, a partir da raiz do repositorio:
- todas as subpastas de `templates/sistema/public` e `templates/sistema/seguro`
  (arquivos do nivel raiz, como `login.html` e `.gitignore`, sao preservados);
- `static/<nome>` de cada app (descobertos, subpastas de template removidas e
  o legado `app`);
- `frontend/<area>/<nome>/dist` de cada app.

Nunca remove `.gitignore`.
"""

import shutil
import sys
from pathlib import Path

from apps_front import AREAS, STATIC_DIR, TEMPLATES_SISTEMA, App, ErroApps, descobrir_apps
from cores import BLUE_COLOR, CYAN_COLOR, RED_COLOR, RESET_COLOR

LEGADO_STATIC = "app"


def log(msg: str, color: str = "") -> None:
    print(f"{color}{msg}{RESET_COLOR}" if color else msg)


def _remover(caminho: Path) -> None:
    if not caminho.exists() and not caminho.is_symlink():
        return
    log(f"==> Removendo {caminho}", BLUE_COLOR)
    if caminho.is_dir() and not caminho.is_symlink():
        shutil.rmtree(caminho)
    else:
        caminho.unlink()


def limpar(apps: list[App] | None = None) -> bool:
    """Remove templates, static e dist gerados. Retorna False em erro de E/S."""
    try:
        if apps is None:
            apps = descobrir_apps()

        nomes = {app.nome for app in apps} | {LEGADO_STATIC}
        subpastas: list[Path] = []
        for area in AREAS:
            base = TEMPLATES_SISTEMA / area
            if base.is_dir():
                subpastas += [p for p in sorted(base.iterdir()) if p.is_dir() and not p.is_symlink()]
        nomes |= {p.name for p in subpastas}

        for pasta in subpastas:
            _remover(pasta)

        for nome in sorted(nomes):
            if nome.startswith("."):
                continue
            _remover(STATIC_DIR / nome)

        for app in apps:
            _remover(app.dist)
    except OSError as erro:
        log(f"Erro ao limpar arquivos anteriores: {erro}", RED_COLOR)
        return False

    return True


def main() -> None:
    try:
        apps = descobrir_apps()
    except ErroApps as erro:
        log(f"Erro: {erro}", RED_COLOR)
        sys.exit(1)

    if not limpar(apps):
        sys.exit(1)

    log("Limpeza concluida.", CYAN_COLOR)
    sys.exit(0)


if __name__ == "__main__":
    main()
