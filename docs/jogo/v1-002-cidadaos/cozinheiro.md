# Cozinheiro

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Cozinheiro trabalha em Cozinhas e Estalagens, processando alimentos brutos em Refeições e servindo viajantes.

## Características ligadas

- **Velocidade (VEL)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Cozinheiro.
- **Carisma (CAR)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Cozinheiro.

## Onde trabalha

- Cozinha: processamento de alimentos em Refeições.
- Estalagem: servir Refeições a viajantes (seção 4.10).

## Ferramenta

- **Cutelo** (seção 7.9): oficina Ferraria; receita 1 Ferro; efeito +L PE de Cozinheiro.

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: +1 PE a cada 24 turnos trabalhando em Cozinha ou Estalagem.
- XP de Guerreiro: não se aplica.

## Produção/Serviço

**Cozinha** (seção 4.5):
- Receita: **2 Grãos + 1 Carne → 5 Refeição**.
- Ciclos: **2** por trabalhador/turno.
- Produção máxima: 10 Refeição por trabalhador/turno (com eficiência 1,0, N1).

**Estalagem** (seção 4.10):
- Vagas: Cozinheiro ou Comerciante (2/5/10 por nível).
- Serve Refeições: por turno consome até `5 × Σ eficiência × mult. nível` Refeições e gera **4 Ouro por Refeição** servida.

## Exemplo de PE efetivo e eficiência

**Cenário**: Cozinheiro com VEL 10, CAR 15, PE base 3, com Cutelo L1, em Cozinha N2.
- Bônus VEL: floor(10 ÷ 5) = 2.
- Bônus CAR: floor(15 ÷ 5) = 3.
- Bônus ferramenta: +1 (Cutelo L1).
- PE efetivo: 3 + 2 + 3 + 1 = 9.
- Eficiência: 0,5 + 0,1 × 9 = 1,4.
- Multiplicador nível N2: ×1,2 (seção 4.3).

**Produção Refeições**:
- Ciclos: 2 × 1,4 × 1,2 = 3,36 ciclos/turno.
- Insumo: 6,72 Grãos + 3,36 Carne.
- Produção: 16,8 Refeição/turno.

**Na Estalagem N2**:
- Capacidade de consumo: `5 × 1,4 × 1,2 = 8,4` Refeições/turno.
- Ouro gerado: 8,4 × 4 = 33,6 Ouro/turno.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Cozinha, Estalagem
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — produção de Refeições; alimentação
- [comerciante.md](comerciante.md) — pode trabalhar na Estalagem (ocupam vagas juntos)
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — usa Cutelo
