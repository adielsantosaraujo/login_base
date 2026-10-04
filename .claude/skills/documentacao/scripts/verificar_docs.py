#!/usr/bin/env python3
"""Verifica a documentacao em docs/ (estrutura, nomes, front matter, links, indices).

Uso: python3 verificar_docs.py [--raiz <dir>]
Saida: arquivo:linha: mensagem; resumo final; exit 1 se houver erro.
Somente stdlib.
"""
import argparse
import re
import sys
from datetime import date
from pathlib import Path

PUBLICOS = ("desenvolvimento", "operacao", "usuario", "negocio")
PASTA_TIPO = {
    "tutoriais": "tutorial",
    "guias": "guia",
    "referencia": "referencia",
    "explicacoes": "explicacao",
    "decisoes": "adr",
}
TIPOS_PASTA = ("tutoriais", "guias", "referencia", "explicacoes")
RE_KEBAB = re.compile(r"^[a-z0-9]+(-[a-z0-9]+)*\.md$")
RE_ADR = re.compile(r"^(\d{4})-[a-z0-9]+(-[a-z0-9]+)*\.md$")
RE_LINK = re.compile(r"\[[^\]]*\]\(\s*<?([^)\s>]+)>?(?:\s+(?:\"[^\"]*\"|'[^']*'))?\s*\)")
RE_FENCE = re.compile(r"^\s{0,3}(`{3,}|~{3,})")
RE_DATA = re.compile(r"^\d{4}-\d{2}-\d{2}$")


def pastas_permitidas():
    ok = {Path("docs")}
    for p in PUBLICOS:
        ok.add(Path("docs") / p)
        for t in TIPOS_PASTA:
            ok.add(Path("docs") / p / t)
    ok.add(Path("docs/desenvolvimento/explicacoes/decisoes"))
    return ok


def parse_front_matter(linhas):
    """Retorna (dict, linha_fim) ou (None, 0). linha_fim e o indice (0-based) do '---' final."""
    if not linhas or linhas[0].strip() != "---":
        return None, 0
    fim = None
    for i in range(1, len(linhas)):
        if linhas[i].strip() == "---":
            fim = i
            break
    if fim is None:
        return None, 0
    dados, chave = {}, None
    for i in range(1, fim):
        bruta = linhas[i].rstrip()
        if not bruta.strip() or bruta.lstrip().startswith("#"):
            continue
        m_item = re.match(r"^\s+-\s*(.*)$", bruta) or re.match(r"^-\s*(.*)$", bruta)
        if m_item and chave is not None and isinstance(dados.get(chave), list):
            dados[chave].append(limpa(m_item.group(1)))
            continue
        m = re.match(r"^([A-Za-z_][\w-]*)\s*:\s*(.*)$", bruta)
        if not m:
            continue
        chave, val = m.group(1), m.group(2)
        val = re.sub(r"\s+#.*$", "", val).strip()
        if val == "":
            dados[chave] = []
        elif val == "[]":
            dados[chave] = []
        else:
            dados[chave] = limpa(val)
    return dados, fim


def limpa(v):
    v = re.sub(r"\s+#.*$", "", v).strip()
    if len(v) >= 2 and v[0] == v[-1] and v[0] in "\"'":
        v = v[1:-1]
    return v


def varre_codigo(linhas):
    """Retorna (fora_de_codigo: list[(n, texto)], erros_fence: list[n])."""
    fora, abertos = [], []
    fence = None  # (char, tam, linha)
    for n, lin in enumerate(linhas, 1):
        m = RE_FENCE.match(lin)
        if fence is None:
            if m:
                fence = (m.group(1)[0], len(m.group(1)), n)
                continue
            fora.append((n, lin))
        else:
            if m and m.group(1)[0] == fence[0] and len(m.group(1)) >= fence[1] \
                    and not lin.strip().strip(fence[0]):
                fence = None
    if fence is not None:
        abertos.append(fence[2])
    return fora, abertos


def remove_inline(texto):
    return re.sub(r"(`+)(.+?)\1", "", texto)


def links_do_arquivo(linhas):
    fora, _ = varre_codigo(linhas)
    for n, lin in fora:
        for m in RE_LINK.finditer(remove_inline(lin)):
            yield n, m.group(1)


def raiz_padrao():
    """Sobe a partir deste arquivo ate achar .git ou pom.xml; fallback: parents[4]."""
    aqui = Path(__file__).resolve()
    for d in aqui.parents:
        if (d / ".git").exists() or (d / "pom.xml").is_file():
            return d
    return aqui.parents[4]


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--raiz", default=None, help="raiz do repositorio")
    args = ap.parse_args()
    raiz = Path(args.raiz).resolve() if args.raiz else raiz_padrao()
    docs = raiz / "docs"
    if not docs.is_dir():
        print("nada a verificar (docs/ nao existe)")
        return 0

    erros = []

    def erro(arq, linha, msg):
        try:
            rel = Path(arq).relative_to(raiz).as_posix()
        except ValueError:
            rel = str(arq)
        erros.append(f"{rel}:{linha}: {msg}")

    arquivos = sorted(p for p in docs.rglob("*.md") if p.is_file())
    permitidas = pastas_permitidas()

    # 2. pastas
    for d in sorted(p for p in docs.rglob("*") if p.is_dir()):
        rel = d.relative_to(raiz)
        if rel not in permitidas:
            erro(d, 1, "pasta fora do conjunto permitido")
        if not any(d.iterdir()):
            erro(d, 1, "pasta vazia")

    adr_numeros = {}
    linkados = {}  # README -> set de alvos resolvidos
    textos = {}
    for arq in arquivos:
        try:
            linhas = arq.read_text(encoding="utf-8").splitlines()
        except (OSError, UnicodeDecodeError) as e:
            erro(arq, 1, f"nao foi possivel ler: {e}")
            continue
        textos[arq] = linhas
        rel = arq.relative_to(docs)
        partes = rel.parts
        indice_ok = rel.as_posix() in ("README.md", "desenvolvimento/explicacoes/decisoes/README.md")
        eh_readme = arq.name == "README.md" and indice_ok
        if arq.name == "README.md" and not indice_ok:
            erro(arq, 1, "indice nao permitido; use docs/README.md")
            continue
        em_decisoes = len(partes) >= 4 and partes[2] == "decisoes" and partes[0] == "desenvolvimento" \
            and partes[1] == "explicacoes"

        # 1. nomes
        if not eh_readme:
            if em_decisoes:
                m = RE_ADR.match(arq.name)
                if not m:
                    erro(arq, 1, "nome de ADR deve seguir NNNN-slug.md (kebab-case ASCII)")
                else:
                    adr_numeros.setdefault(m.group(1), []).append(arq)
            elif not RE_KEBAB.match(arq.name):
                erro(arq, 1, "nome fora de kebab-case ASCII")
        for p in partes[:-1]:
            if not re.match(r"^[a-z0-9]+(-[a-z0-9]+)*$", p):
                erro(arq, 1, f"pasta '{p}' fora de kebab-case ASCII")

        # 8. blocos fechados
        _, abertos = varre_codigo(linhas)
        for n in abertos:
            erro(arq, n, "bloco de codigo (```) nao fechado")

        # 3 e 4. front matter
        if not eh_readme:
            fm, fim = parse_front_matter(linhas)
            if fm is None:
                erro(arq, 1, "front matter ausente ou nao fechado")
            else:
                obrig = ["titulo", "publico", "tipo", "atualizado_em", "fontes"]
                if em_decisoes:
                    obrig += ["status", "data"]
                for k in obrig:
                    if k not in fm:
                        erro(arq, 1, f"front matter sem a chave '{k}'")
                    elif k != "fontes" and not fm[k]:
                        erro(arq, 1, f"chave '{k}' vazia")
                pub_pasta = partes[0] if partes else None
                tipo_pasta = PASTA_TIPO.get(partes[2] if em_decisoes else (partes[1] if len(partes) > 1 else ""))
                if isinstance(fm.get("publico"), str) and fm["publico"] and fm["publico"] != pub_pasta:
                    erro(arq, 1, f"publico '{fm['publico']}' nao bate com a pasta '{pub_pasta}'")
                if isinstance(fm.get("tipo"), str) and fm["tipo"] and tipo_pasta and fm["tipo"] != tipo_pasta:
                    erro(arq, 1, f"tipo '{fm['tipo']}' nao bate com a pasta (esperado '{tipo_pasta}')")
                for k in ("atualizado_em", "data"):
                    v = fm.get(k)
                    if isinstance(v, str) and v:
                        ok = bool(RE_DATA.match(v))
                        if ok:
                            try:
                                date.fromisoformat(v)
                            except ValueError:
                                ok = False
                        if not ok:
                            erro(arq, 1, f"'{k}' deve ser AAAA-MM-DD (valor: '{v}')")
                fontes = fm.get("fontes", [])
                if isinstance(fontes, str):
                    erro(arq, 1, "'fontes' deve ser lista (use 'fontes: []' se vazia)")
                    fontes = []
                for f in fontes:
                    if not f:
                        continue
                    if not (raiz / f).exists():
                        # localiza a linha do item
                        ln = next((i + 1 for i, l in enumerate(linhas[:fim]) if f in l), 1)
                        erro(arq, ln, f"fonte inexistente: {f}")

    # 5. links (docs + README da raiz)
    alvos_links = list(textos.items())
    readme_raiz = raiz / "README.md"
    if readme_raiz.is_file():
        try:
            alvos_links.append((readme_raiz, readme_raiz.read_text(encoding="utf-8").splitlines()))
        except (OSError, UnicodeDecodeError):
            pass
    for arq, linhas in alvos_links:
        for n, alvo in links_do_arquivo(linhas):
            if re.match(r"^(https?:|mailto:)", alvo, re.I) or alvo.startswith("#"):
                continue
            if alvo.startswith("/"):
                erro(arq, n, f"link nao pode comecar com '/': {alvo}")
                continue
            caminho = re.split(r"[#?]", alvo, 1)[0]
            if not caminho:
                continue
            destino = (arq.parent / caminho).resolve()
            if not destino.exists():
                erro(arq, n, f"link quebrado: {alvo}")
                continue
            if arq.name == "README.md" and arq.parent in (docs, docs / "desenvolvimento/explicacoes/decisoes"):
                linkados.setdefault(arq, set()).add(destino)

    # 6. cobertura dos indices
    idx_geral = docs / "README.md"
    idx_adr = docs / "desenvolvimento/explicacoes/decisoes/README.md"
    for arq in arquivos:
        if arq.name == "README.md" and arq in (idx_geral, idx_adr):
            continue
        if arq.name == "README.md":
            continue
        eh_adr = arq.parent == idx_adr.parent
        idx = idx_adr if eh_adr else idx_geral
        if not idx.is_file():
            erro(arq, 1, f"indice ausente: {idx.relative_to(raiz).as_posix()}")
        elif arq.resolve() not in linkados.get(idx, set()):
            erro(arq, 1, f"nao listado em {idx.relative_to(raiz).as_posix()}")

    # 7. ADRs duplicadas
    for num, lista in sorted(adr_numeros.items()):
        if len(lista) > 1:
            for a in lista:
                erro(a, 1, f"numero de ADR duplicado: {num}")

    for e in erros:
        print(e)
    print(f"\n{len(arquivos)} arquivos verificados, {len(erros)} erros")
    return 1 if erros else 0


if __name__ == "__main__":
    sys.exit(main())
