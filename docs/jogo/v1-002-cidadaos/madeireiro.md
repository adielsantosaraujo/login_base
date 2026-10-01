# Madeireiro

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Madeireiro trabalha em Acampamentos de lenhadores, Serrarias e Carpintarias, coletando e processando madeira.

## Características ligadas

- **Força (FOR)** [proposta] (seção 5.2): a cada 5 pontos, +1 PE de Madeireiro.
- **Vitalidade (VIT)** [proposta] (seção 5.2): a cada 5 pontos, +1 PE de Madeireiro.

**Nota**: Madeireiro é ligado a FOR + VIT, uma proposta do planejamento ausente no requisito original. Validar trade-off com profissões rurais.

## Onde trabalha

- Acampamento de lenhadores: coleta Madeira (e Caça com Cabana separada).
- Serraria: produção de Tábuas.
- Carpintaria: oficina de fabricação de ferramentas e armas.

## Ferramenta

- **Machado** (seção 7.9): oficina Ferraria; receita 2 Ferro + 1 Tábua; efeito +L PE de Madeireiro.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em prédio de Madeireiro.
- XP de Guerreiro: não se aplica.

## Produção por coleta/processamento

**Coleta — Acampamento de lenhadores** (seção 4.5):
- Base: **5 Madeira** por trabalhador/turno.

**Processamento — Serraria** (seção 4.5):
- Receita: **2 Madeira → 1 Tábua**.
- Ciclos: **3** por trabalhador/turno.
- Produção máxima: 3 Tábua por trabalhador/turno (com eficiência 1,0, N1).

## Exemplo de PE efetivo e eficiência

**Cenário**: Madeireiro com FOR 12, VIT 10, PE base 4, com Machado L2, em Acampamento N2.
- Bônus FOR: floor(12 ÷ 5) = 2.
- Bônus VIT: floor(10 ÷ 5) = 2.
- Bônus ferramenta: +2 (Machado L2).
- PE efetivo: 4 + 2 + 2 + 2 = 10.
- Eficiência: 0,5 + 0,1 × 10 = 1,5.
- Multiplicador nível N2: ×1,2 (seção 4.3).
- Produção Madeira: 5 × 1,5 × 1,2 = 9 Madeira/turno.

**Na Serraria N2**:
- Ciclos por turno: 3 × 1,5 × 1,2 = 5,4 ciclos.
- Insumo: 10,8 Madeira necessária.
- Produção: 5,4 Tábua/turno.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Acampamento de lenhadores, Serraria, Carpintaria
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de Madeira e Tábua
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — usa Machado
- [armas.md](../v1-004-armas/armas.md) — Carpintaria fabrica armas

## Questões em aberto

- [proposta] FOR + VIT para Madeireiro: validar que não desequilibra carregador (FOR + VEL).
