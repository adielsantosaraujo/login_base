# Spec Delta

## Purpose

Define os tokens de design globais do tema "Vilarejo", o preset PrimeVue personalizado em modo escuro, o cabeçalho do layout do jogo, e o uso consistente dos tokens nas telas do jogo.

## ADDED Requirements

### Requirement: Tokens de design globais
O frontend SHALL definir os tokens do tema "Vilarejo" como variáveis CSS globais com prefixo `--vl-` (cores de fundo, superfícies, borda, textos, destaque, aviso, erro, cores dos 5 tipos de região e dos 13 bônus, fontes e raios), carregar as fontes Bricolage Grotesque, IBM Plex Sans e IBM Plex Mono e aplicar `--vl-bg` como fundo da página.

#### Scenario: Tokens disponíveis
- **WHEN** qualquer tela do SPA é aberta
- **THEN** as variáveis `--vl-*` estão definidas em `:root` e o fundo da página é `--vl-bg`

### Requirement: Preset PrimeVue Vilarejo em modo escuro
O PrimeVue SHALL usar o preset "Vilarejo", derivado do Aura com `definePreset`, com a cor primária igual ao destaque (`--vl-accent`) e superfícies escuras dos tokens, e o modo escuro MUST estar sempre ativo.

#### Scenario: Componente PrimeVue escuro
- **WHEN** uma tela do jogo exibe um componente PrimeVue (por exemplo, Dialog ou DataTable)
- **THEN** o componente usa superfícies escuras e o destaque âmbar do tema

### Requirement: Cabeçalho Vilarejo no layout do jogo
Todas as rotas do jogo SHALL exibir o cabeçalho com o logo "Vilarejo", as abas Mapa, Estoque, Famílias, Mercado, Inventário e Batalhas (a ativa destacada), as pílulas "Turno" e "Próximo turno" (`mm:ss`) e o acesso ao relatório do turno. Nas telas de criação da vila e de distribuição da população, as abas MUST aparecer inativas e não navegar.

#### Scenario: Aba ativa
- **WHEN** o usuário abre `/app/jogo/estoque`
- **THEN** a aba "Estoque" aparece ativa e as pílulas mostram o turno atual e o tempo até o próximo

#### Scenario: Abas inativas no início de jogo
- **WHEN** o usuário está em `/app/jogo/criar-vila`
- **THEN** as abas do cabeçalho não navegam

### Requirement: Todas as telas do jogo usam os tokens
Todas as views e componentes do jogo SHALL usar os tokens `--vl-*` (ou o preset) para cores, fontes e raios, sem cores fixas de tema claro, mantendo textos, comportamento e contratos.

#### Scenario: Sem cores fixas
- **WHEN** os arquivos `.vue` do jogo são inspecionados
- **THEN** não há cores hexadecimais ou rgb de tema claro nos estilos, exceto valores definidos nos tokens ou no preset
