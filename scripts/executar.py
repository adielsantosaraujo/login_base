#!/usr/bin/env python3
"""Lista os alvos numerados do Makefile e executa o escolhido.

Equivalente Python ao alvo `executar` (`make e`) do Makefile: le os
alvos com comentario `## descricao`, exibe um menu numerado, pede a
escolha e roda `make <alvo>` no alvo selecionado.
"""

import re
import subprocess
import sys
from pathlib import Path

from cores import CYAN_COLOR, RESET_COLOR, YELLOW_COLOR

MAKEFILE = Path(__file__).resolve().parent.parent / "Makefile"
ALVO_RE = re.compile(r"^([a-zA-Z0-9_-]+):.*?## (.*)$")


def listar_alvos() -> list[tuple[str, str]]:
    alvos = []
    for linha in MAKEFILE.read_text().splitlines():
        m = ALVO_RE.match(linha)
        if m and m.group(1) != "executar":
            alvos.append((m.group(1), m.group(2)))
    return alvos


def main() -> None:
    alvos = listar_alvos()

    margem = 4
    largura = max([len(nome) for nome, _ in alvos] + [len("sair")]) + margem

    def linha(indice: int, nome: str, desc: str) -> str:
        tracos = "-" * (largura - len(nome) - 1)
        return (
            f"  {CYAN_COLOR}{indice:>3})  {YELLOW_COLOR}{nome}{RESET_COLOR} "
            f"{tracos} {desc}"
        )

    print()
    print(linha(0, "sair", "Encerra o menu"))
    for i, (nome, desc) in enumerate(alvos, start=1):
        print(linha(i, nome, desc))
    print()

    escolha = input("Escolha um numero: ").strip()

    if escolha == "0":
        print("Saindo.")
        return

    if not escolha.isdigit() or not (1 <= int(escolha) <= len(alvos)):
        print("Opcao invalida.")
        return

    cmd, _ = alvos[int(escolha) - 1]
    print()
    print(f"==> Executando: make {cmd}")
    sys.exit(subprocess.call(["make", cmd]))


if __name__ == "__main__":
    main()
