#!/usr/bin/env python3
"""Descoberta dos apps frontend (Vue/Vite) do projeto.

Cada app fica em `frontend/public/<nome>/` ou `frontend/seguro/<nome>/` e e
identificado pela presenca de um `package.json`. Os caminhos sao relativos a
raiz do repositorio (os scripts sao executados a partir dela).
"""

import re
from dataclasses import dataclass
from pathlib import Path

AREAS = ("public", "seguro")
TEMPLATES_SISTEMA = Path("src/main/resources/templates/sistema")
STATIC_DIR = Path("src/main/resources/static")
FRONTEND_DIR = Path("frontend")

NOME_VALIDO = re.compile(r"^[a-z0-9_-]+$")
NOMES_RESERVADOS = frozenset(
    {"css", "js", "images", "login", "logout", "api", "error", "v3", "swagger-ui", "app"}
)


class ErroApps(ValueError):
    """Configuracao invalida dos apps frontend."""


@dataclass(frozen=True)
class App:
    nome: str
    area: str
    dir: Path

    @property
    def dist(self) -> Path:
        return self.dir / "dist"

    @property
    def static_dir(self) -> Path:
        return STATIC_DIR / self.nome

    @property
    def template(self) -> Path:
        return TEMPLATES_SISTEMA / self.area / self.nome / "index.html"


def descobrir_apps() -> list[App]:
    """Lista os apps em frontend/<area>/*/package.json, ordenados por area/nome."""
    apps: list[App] = []
    for area in AREAS:
        for pkg in sorted((FRONTEND_DIR / area).glob("*/package.json")):
            apps.append(App(nome=pkg.parent.name, area=area, dir=pkg.parent))

    vistos: dict[str, App] = {}
    for app in apps:
        if not NOME_VALIDO.match(app.nome):
            raise ErroApps(
                f"Nome de app invalido '{app.nome}' ({app.dir}): use apenas [a-z0-9_-]."
            )
        if app.nome in NOMES_RESERVADOS:
            raise ErroApps(f"Nome de app reservado '{app.nome}' ({app.dir}).")
        if app.nome in vistos:
            raise ErroApps(
                f"Nome de app duplicado '{app.nome}': {vistos[app.nome].dir} e {app.dir}."
            )
        vistos[app.nome] = app

    return sorted(apps, key=lambda a: (a.area, a.nome))
