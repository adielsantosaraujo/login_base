-- Itens fabricados e fabricações em andamento nas oficinas.

create table item (
    id                 bigint generated always as identity,
    vila_id            bigint not null,
    categoria          varchar(20) not null,
    subtipo            varchar(30) not null,
    qualidade          varchar(20) not null,
    nivel              int not null,
    bonus              jsonb not null default '[]',
    atributo_escolhido varchar(3) null,
    cidadao_id         bigint null,
    slot               varchar(20) null,
    em_aprimoramento   boolean not null default false,
    criado_em          timestamptz not null default current_timestamp,
    constraint pk_item primary key (id),
    constraint fk_item_vila foreign key (vila_id) references vila (id),
    constraint fk_item_cidadao foreign key (cidadao_id) references cidadao (id),
    constraint ck_item_nivel check (nivel between 1 and 10),
    constraint ck_item_cidadao_slot check ((cidadao_id is null) = (slot is null))
);

create index ix_item_vila on item (vila_id);
create index ix_item_vila_categoria on item (vila_id, categoria);
create index ix_item_cidadao on item (cidadao_id);
create unique index ux_item_cidadao_slot on item (cidadao_id, slot) where cidadao_id is not null;

create table fabricacao (
    id                 bigint generated always as identity,
    vila_id            bigint not null,
    construcao_id      bigint not null,
    artesao_id         bigint not null,
    subtipo            varchar(30) not null,
    nivel              int not null,
    atributo_escolhido varchar(3) null,
    item_id            bigint null,
    pf_total           int not null,
    pf_atual           numeric(10,4) not null default 0,
    estado             varchar(20) not null,
    turno_inicio       int not null,
    criado_em          timestamptz not null default current_timestamp,
    constraint pk_fabricacao primary key (id),
    constraint fk_fabricacao_vila foreign key (vila_id) references vila (id),
    constraint fk_fabricacao_construcao foreign key (construcao_id) references construcao (id),
    constraint fk_fabricacao_artesao foreign key (artesao_id) references cidadao (id),
    constraint fk_fabricacao_item foreign key (item_id) references item (id),
    constraint ck_fabricacao_nivel check (nivel between 1 and 10)
);

create unique index ux_fabricacao_artesao on fabricacao (artesao_id);
create index ix_fabricacao_construcao on fabricacao (construcao_id);
create index ix_fabricacao_vila on fabricacao (vila_id);
