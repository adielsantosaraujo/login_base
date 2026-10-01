-- Alocação de trabalhadores: fonte única em cidadao.construcao_id + profissão exercida no prédio.
alter table cidadao add column profissao_trabalho varchar(30);
create index if not exists ix_cidadao_construcao on cidadao (construcao_id);
