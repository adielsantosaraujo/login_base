#!/usr/bin/env python3
"""Cores de terminal usadas pelos scripts in.py e logs.py.

Equivalente Python ao trecho shell:

    RESET_COLOR=$(tput sgr0)
    RED_COLOR=$(tput setaf 1)
    YELLOW_COLOR=$(tput setaf 3)
    BLUE_COLOR=$(tput setaf 4)
    CYAN_COLOR=$(tput setaf 6)

Mesmo esquema de cores usado no menu do Makefile ("make e"): numero
em ciano e nome em amarelo.

Se o 'tput' nao estiver disponivel ou nao retornar nada (TERM ausente
ou sem suporte a cores), cai para os codigos ANSI equivalentes.
"""

import subprocess

_ANSI_FALLBACK = {
    ("sgr0",): "\033[0m",
    ("setaf", "1"): "\033[31m",
    ("setaf", "3"): "\033[33m",
    ("setaf", "4"): "\033[34m",
    ("setaf", "6"): "\033[36m",
}


def _tput(*args: str) -> str:
    try:
        saida = subprocess.run(["tput", *args], capture_output=True, text=True, check=True)
        if saida.stdout:
            return saida.stdout
    except (FileNotFoundError, subprocess.CalledProcessError):
        pass
    return _ANSI_FALLBACK.get(args, "")


RESET_COLOR = _tput("sgr0")
RED_COLOR = _tput("setaf", "1")
YELLOW_COLOR = _tput("setaf", "3")
BLUE_COLOR = _tput("setaf", "4")
CYAN_COLOR = _tput("setaf", "6")
