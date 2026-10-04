# Design tokens

Os valores de origem estão em oklch (nos protótipos); os hex abaixo são aproximados. Sugestão: criar variáveis CSS globais ou um preset do PrimeVue (tema escuro) com estes valores.

## Cores

| Token | oklch | Hex aprox. | Uso |
|---|---|---|---|
| `--bg` | 0.16 0.01 60 | `#1a1714` | Fundo da página |
| `--surface-1` | 0.19 0.012 60 | `#211d19` | Header, container do mapa |
| `--surface-2` | 0.20 0.012 60 | `#24201c` | Cards do painel e de cidadão |
| `--surface-3` | 0.22 0.012 60 | `#27231f` | Tiles, botões secundários |
| `--surface-4` | 0.24–0.27 0.012 60 | `#2b2622`–`#332e29` | Pílulas, steppers, trilhos de barra |
| `--border` | 0.27–0.30 0.012 60 | `#36302a`–`#3d3731` | Bordas |
| `--text` | 0.94 0.01 80 | `#f2eee8` | Texto principal |
| `--text-2` | 0.72 0.015 70 | `#b0a89e` | Texto secundário, rótulos |
| `--text-3` | 0.58–0.62 0.012 70 | `#8d867c`–`#958d83` | Texto desativado ou valor 0 |
| `--accent` | 0.80 0.13 75 | `#e8b55a` | Destaque, CTA, seleção |
| `--accent-bg` | accent / 10–14% | — | Fundo do item selecionado |
| `--accent-ink` | 0.18 0.01 60 | `#1d1a17` | Texto sobre o destaque |
| `--warn` | 0.78 0.10 60 | `#d9a06a` | Pontos pendentes |
| `--error` | 0.72 0.15 30 | `#e07a5f` | Abaixo do mínimo |

### Tipos de região

| Tipo | Hex aprox. |
|---|---|
| Floresta | `#6cbf72` |
| Planície | `#b3d36a` |
| Urbana | `#e8b55a` |
| Litoral | `#8fcde0` |
| Montanha | `#a9afba` |

### Bônus de região

| Bônus | oklch |
|---|---|
| Floresta | 0.72 0.13 145 |
| Barreiro | 0.68 0.11 45 |
| Plantações | 0.80 0.13 120 |
| Criações | 0.76 0.09 70 |
| Rocha | 0.74 0.02 250 |
| Ferro | 0.66 0.07 230 |
| Carvão | 0.60 0.01 260 |
| Salinas | 0.86 0.04 210 |
| Enxofre | 0.86 0.14 100 |
| Militar | 0.66 0.13 20 |
| Indústria | 0.72 0.11 50 |
| Comércio | 0.80 0.13 75 |
| Desenvolvimento | 0.76 0.11 290 |

## Tipografia (Google Fonts)

| Uso | Fonte | Tamanho / peso |
|---|---|---|
| H1 | Bricolage Grotesque | `clamp(32px, 4.5vw, 48px)` / 700, letter-spacing −0.02em |
| H2 de card | Bricolage Grotesque | 20px / 700 |
| Nome do cidadão, abas | Bricolage Grotesque | 17–18px / 700 |
| Texto | IBM Plex Sans | 13–16px / 400–600 |
| Rótulo de seção | IBM Plex Sans | 11–12px, maiúsculas, letter-spacing .08em |
| Números, contadores | IBM Plex Mono | 12–18px / 400–500 |

## Raios
6px (chips), 8px (abas de navegação), 10px (slots, opções), 12px (tiles, botões CTA), 14px (cards de cidadão), 16px (cards do painel), 999px (pílulas e botões arredondados).

## Componentes comuns

- **Header**: logo "Vilarejo" e navegação em abas (a ativa tem fundo `--surface-4`). À direita, as pílulas "TURNO" e "PRÓXIMO TURNO" (`mm:ss` na cor `--accent`).
- **Stepper**: botões − e + de 20–24px, raio 5–6px, fundo `--surface-4`, com o valor em Mono no centro. Desativado: opacidade .25–.3.
- **Barra**: altura 3–6px, trilho `--surface-4`, preenchimento na cor do item e transição de largura de .15–.25s.
- **Checklist**: círculo de 18px, preenchido com `--accent` e "✓" quando a condição é cumprida, `--warn` com "!" para aviso e `--surface-4` vazio quando não cumprida.
- **CTA**: altura 48px, raio 12px, 15px 600. Ativo: fundo `--accent` e texto `--accent-ink`. Inativo: fundo `--surface-4`, texto `--text-3` e cursor `not-allowed`.
- **Toast**: fixo no rodapé central (bottom 28px), fundo `--accent`, raio 12px, sombra `0 10px 30px rgba(0,0,0,.4)`.
