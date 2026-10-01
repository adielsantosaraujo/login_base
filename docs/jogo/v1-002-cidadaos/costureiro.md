# Costureiro

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Costureiro trabalha em Tecelagens, Curtumes e Alfaiatarias, processando fibras em tecidos e fabricando roupas.

## Características ligadas

- **Velocidade (VEL)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Costureiro.
- **Carisma (CAR)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Costureiro.

## Onde trabalha

- Tecelagem: processamento de fibras em Tecido.
- Curtume: processamento de couro em Couro curtido.
- Alfaiataria (oficina): fabricação de roupas (parte das armaduras).

## Ferramenta

- **Kit de costura** (seção 7.9): oficina Alfaiataria; receita 1 Ferro + 1 Tecido; efeito +L PE de Costureiro.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em Tecelagem, Curtume ou Alfaiataria.
- XP de Guerreiro: não se aplica.

## Produção/Fabricação

**Tecelagem** (seção 4.5):
- Receita: **2 Fibra OU 2 Lã → 1 Tecido**.
- Ciclos: **2** por trabalhador/turno.
- Produção máxima: 2 Tecido por trabalhador/turno (com eficiência 1,0, N1).

**Curtume** (seção 4.5):
- Receita: **2 Couro + 1 Sal → 1 Couro curtido**.
- Ciclos: **2** por trabalhador/turno.
- Produção máxima: 2 Couro curtido por trabalhador/turno (com eficiência 1,0, N1).

**Alfaiataria (oficina)** (seção 4.11):
- Nível máximo de item fabricável: N1 até L3; N2 até L6; N3 até L10.
- Vagas de artesão: 2/5/10.
- Fabrica armaduras (Luvas, Calças, Sapato) e ferramentas (Kit de costura).

## Exemplo de PE efetivo e eficiência

**Cenário**: Costureiro com VEL 12, CAR 14, PE base 4, com Kit de costura L2, em Tecelagem N2.
- Bônus VEL: floor(12 ÷ 5) = 2.
- Bônus CAR: floor(14 ÷ 5) = 2.
- Bônus ferramenta: +2 (Kit L2).
- PE efetivo: 4 + 2 + 2 + 2 = 10.
- Eficiência: 0,5 + 0,1 × 10 = 1,5.
- Multiplicador nível N2: ×1,2 (seção 4.3).

**Produção Tecido**:
- Ciclos: 2 × 1,5 × 1,2 = 3,6 ciclos/turno.
- Insumo: 7,2 Fibra (ou Lã).
- Produção: 3,6 Tecido/turno.

**Em Alfaiataria N3 (fabricação)**:
- Cria até L10 (nível máximo).
- Com eficiência 1,5, pode fabricar itens de nível até L8 (requisito 2L − 2 ≤ PE efetivo).

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Tecelagem, Curtume, Alfaiataria
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de Tecido, Couro curtido
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — fabrica Kit de costura; usa Kit
- [armaduras.md](../v1-006-Armaduras/armaduras.md) — fabrica armaduras (Alfaiataria)
