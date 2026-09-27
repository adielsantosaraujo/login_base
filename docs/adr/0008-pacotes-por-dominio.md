# 0008 — Pacotes Organizados por Domínio de Negócio

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-24 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-user-authentication`](../../openspec/changes/archive/2026-09-25-add-user-authentication/), [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

Código Java precisa de organização em packages. Há duas estratégias principais: por **camada técnica** (`entity`, `repository`, `service`, `controller`) ou por **domínio de negócio** (`acesso`, `seguranca`, `auditoria`, `jogo`, `jogo.economia`, etc.). Com várias mudanças (autenticação, jogo) sendo desenvolvidas em paralelo, isolamento por domínio melhora clareza e reduz acoplamento.

## Direcionadores da decisão

- Múltiplas mudanças futuras (login, jogo, etc.) — evitar "package hell" de camadas
- Rastreabilidade: quem toca `acesso/*` sabe que trabalha com usuários/permissões
- Manutenibilidade: deletar um domínio = deletar uma pasta (vs. espalhado por camadas)
- Coesão: `acesso.Usuario` + `acesso.UsuarioRepository` + `acesso.UsuarioService` juntos
- Escalabilidade: `jogo` com subpacotes (`jogo.economia`, `jogo.construcao`, etc.)

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Pacotes por domínio** | `acesso`, `seguranca`, `auditoria`, `web`, `jogo`, `jogo.economia`, `jogo.construcao`, etc. Entidades, repositórios, serviços juntos. |
| Pacotes por camada | `entity`, `repository`, `service`, `controller`. Spread por camadas. Rejeitada: perde isolamento entre domínios. |
| Pacotes por feature | `authentication`, `village`, `farming`. Híbrido. Menos comum, não pedido. |

## Resultado da decisão

Adotou-se **organização por domínio**:

```
com.example.loginbase/
├── auditoria/              → Auditoria, auditor-aware
├── acesso/                 → Usuário, perfil, permissão, sessão
├── seguranca/              → Spring Security, login, handlers
├── web/                    → Controllers de páginas (login, index)
├── jogo/                   → Exceções e enums genéricos do jogo
├── jogo.api/               → Controllers do jogo, DTOs, mappers
├── jogo.dominio/           → Entidades do jogo (Vila, Prédio, etc.)
├── jogo.catalogo/          → Enums/records de catálogo (TipoPredio, etc.)
├── jogo.config/            → Beans (Clock, Aleatorio), JogoProperties
├── jogo.economia/          → Cálculos de produção, VilaService
├── jogo.construcao/        → Lógica de construção, ConstrucaoService
├── jogo.fazenda/           → Lógica da fazenda, FazendaService
├── jogo.forja/             → Lógica de forja, ForjaService
├── jogo.quartel/           → Lógica de quartel, QuartelService
├── jogo.masmorra/          → Lógica de masmorras, MasmorraService
├── jogo.masmorra.combate/  → Motor de combate (puro), IA
└── suporte/                → Testes: Clock mutável, Aleatorio sequencial
```

### Consequências positivas
- **Coesão alta**: tudo de um domínio junto (entidade, repo, service)
- **Baixo acoplamento**: trocar `acesso` não afeta `jogo`
- **Rastreabilidade**: saber quem toca o quê é trivial
- **Escalabilidade**: `jogo.*` cresce sem afetar `acesso` ou `seguranca`
- **Deletável**: remover um domínio = rm -rf (vs. procura por camada)

### Consequências negativas
- **Menos convencional**: nem todo desenvolvedor espera essa organização
- **Mistura de camadas**: uma package tem entity + service (não é "layered")
- **Reuso transversal**: utilidades acabam em pacotes ambíguos (mitigado com `jogo.suporte`, `acesso.NormalizacaoContato`)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Pacotes por domínio | Coesão alta; isolamento entre domínios; escalável; claro. | Menos convencional; mistura de camadas. |
| Pacotes por camada | Convencional (MVC clássico). | Espalhado; acoplamento alto com crescimento. |

## Mais informações

- **Design (auth)**: [`add-user-authentication/design.md` §Decisions 1](../../openspec/changes/archive/2026-09-25-add-user-authentication/design.md)
- **Design (jogo)**: [`add-city-builder-game/design.md` §12](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Código**: [`src/main/java/com/example/loginbase/`](../../src/main/java/com/example/loginbase/) — hierarquia de pacotes

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
