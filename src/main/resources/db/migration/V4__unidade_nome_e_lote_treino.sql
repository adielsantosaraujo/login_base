-- Nomes de unidade (nome/sobrenome/ordinal_nome), contador histórico de nomes
-- por vila e vínculo de itens reservados à ordem (suporte a treino em lote):
-- ver design.md da change add-soldier-names-batch-slots (decisões D3 e D11).

-- 1) nome/sobrenome em jogo_unidades, com backfill determinístico a partir
--    dos 10 primeiros nomes/sobrenomes do JSON de nomes (D3).
alter table jogo_unidades add column nome varchar(60);
alter table jogo_unidades add column sobrenome varchar(60);

update jogo_unidades
set nome = (array['Ana', 'Maria', 'Francisca', 'Antônia', 'Adriana', 'Juliana', 'Márcia', 'Fernanda', 'Patrícia', 'Aline'])[1 + id % 10],
    sobrenome = (array['Silva', 'Santos', 'Oliveira', 'Souza', 'Rodrigues', 'Ferreira', 'Alves', 'Pereira', 'Lima', 'Gomes'])[1 + (id / 10) % 10];

alter table jogo_unidades alter column nome set not null;
alter table jogo_unidades alter column sobrenome set not null;

-- 2) ordem_id em jogo_itens, vinculando itens reservados à ordem de treino
--    em lote que os reservou.
alter table jogo_itens add column ordem_id bigint;
alter table jogo_itens add constraint fk_jogo_itens_ordem foreign key (ordem_id) references jogo_ordens (id);

create index ix_jogo_itens_ordem on jogo_itens (ordem_id);

-- Migra ordens TREINO em andamento: liga os itens já reservados (arma e
-- armadura) da ordem via a nova coluna ordem_id, antes de remover as
-- colunas arma_item_id/armadura_item_id de jogo_ordens.
update jogo_itens i
set ordem_id = o.id
from jogo_ordens o
where o.categoria = 'TREINO' and i.id in (o.arma_item_id, o.armadura_item_id);

-- 3) Remove o vínculo antigo (arma_item_id/armadura_item_id) de jogo_ordens;
--    o vínculo agora é jogo_itens.ordem_id.
alter table jogo_ordens drop constraint fk_jogo_ordens_arma_item;
alter table jogo_ordens drop constraint fk_jogo_ordens_armadura_item;
alter table jogo_ordens drop column arma_item_id;
alter table jogo_ordens drop column armadura_item_id;

-- 4) ordinal_nome em jogo_unidades (D11): posição da unidade entre as
--    unidades da mesma vila com o mesmo par nome/sobrenome, na ordem de id.
alter table jogo_unidades add column ordinal_nome int;

update jogo_unidades u
set ordinal_nome = r.ordinal
from (
    select id, row_number() over (partition by vila_id, nome, sobrenome order by id) as ordinal
    from jogo_unidades
) r
where r.id = u.id;

alter table jogo_unidades alter column ordinal_nome set not null;
alter table jogo_unidades add constraint ck_jogo_unidades_ordinal_nome check (ordinal_nome >= 1);
alter table jogo_unidades add constraint uk_jogo_unidades_nome_ordinal unique (vila_id, nome, sobrenome, ordinal_nome);

-- 5) jogo_contadores_nome: contador histórico do maior ordinal já atribuído
--    a cada par nome/sobrenome por vila (D11). Nunca decrementa quando
--    unidades morrem.
create table jogo_contadores_nome (
    id             bigint generated always as identity,
    vila_id        bigint not null,
    nome           varchar(60) not null,
    sobrenome      varchar(60) not null,
    ultimo_ordinal int not null,
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_jogo_contadores_nome primary key (id),
    constraint fk_jogo_contadores_nome_vila foreign key (vila_id) references jogo_vilas (id),
    constraint uk_jogo_contadores_nome unique (vila_id, nome, sobrenome),
    constraint ck_jogo_contadores_nome_ultimo check (ultimo_ordinal >= 1)
);

insert into jogo_contadores_nome (vila_id, nome, sobrenome, ultimo_ordinal, criado_em, criado_por, alterado_em, alterado_por)
select vila_id, nome, sobrenome, count(*), now(), 'sistema', now(), 'sistema'
from jogo_unidades
group by vila_id, nome, sobrenome;
