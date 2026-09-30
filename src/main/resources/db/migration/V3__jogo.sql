-- Esquema do jogo (city builder): vila, prédios, fazenda, itens, tropas, ordens de fila
-- e batalhas em masmorra. Todas as tabelas têm auditoria (criado_em/criado_por/alterado_em/
-- alterado_por), preenchida pela aplicação (Spring Data JPA Auditing).

create table jogo_vilas (
    id                        bigint generated always as identity,
    usuario_id                bigint not null,
    nome                      varchar(100) not null,
    comida                    bigint not null,
    madeira                   bigint not null,
    pedra                     bigint not null,
    ferro                     bigint not null,
    recursos_atualizados_em   timestamptz not null,
    masmorra_nivel_liberado   int not null default 1,
    criado_em                 timestamptz not null,
    criado_por                varchar(150) not null,
    alterado_em               timestamptz not null,
    alterado_por              varchar(150) not null,
    constraint pk_jogo_vilas primary key (id),
    constraint fk_jogo_vilas_usuario foreign key (usuario_id) references usuarios (id),
    constraint uk_jogo_vilas_usuario unique (usuario_id),
    constraint ck_jogo_vilas_recursos check (comida >= 0 and madeira >= 0 and pedra >= 0 and ferro >= 0),
    constraint ck_jogo_vilas_masmorra_nivel check (masmorra_nivel_liberado between 1 and 5)
);

create table jogo_predios (
    id             bigint generated always as identity,
    vila_id        bigint not null,
    tipo           varchar(30) not null,
    nivel          int not null,
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_jogo_predios primary key (id),
    constraint fk_jogo_predios_vila foreign key (vila_id) references jogo_vilas (id),
    constraint uk_jogo_predios_vila_tipo unique (vila_id, tipo),
    constraint ck_jogo_predios_nivel check (nivel between 0 and 5)
);

create index ix_jogo_predios_vila on jogo_predios (vila_id);

create table jogo_canteiros (
    id             bigint generated always as identity,
    vila_id        bigint not null,
    posicao        int not null,
    cultivo        varchar(30) not null,
    plantado_em    timestamptz not null,
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_jogo_canteiros primary key (id),
    constraint fk_jogo_canteiros_vila foreign key (vila_id) references jogo_vilas (id),
    constraint uk_jogo_canteiros_vila_posicao unique (vila_id, posicao),
    constraint ck_jogo_canteiros_posicao check (posicao between 1 and 5)
);

create index ix_jogo_canteiros_vila on jogo_canteiros (vila_id);

create table jogo_sementes (
    id             bigint generated always as identity,
    vila_id        bigint not null,
    cultivo        varchar(30) not null,
    quantidade     int not null,
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_jogo_sementes primary key (id),
    constraint fk_jogo_sementes_vila foreign key (vila_id) references jogo_vilas (id),
    constraint uk_jogo_sementes_vila_cultivo unique (vila_id, cultivo),
    constraint ck_jogo_sementes_quantidade check (quantidade >= 0)
);

create index ix_jogo_sementes_vila on jogo_sementes (vila_id);

create table jogo_itens (
    id             bigint generated always as identity,
    vila_id        bigint not null,
    modelo         varchar(30) not null,
    nivel          int not null,
    origem         varchar(20) not null,
    status         varchar(20) not null,
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_jogo_itens primary key (id),
    constraint fk_jogo_itens_vila foreign key (vila_id) references jogo_vilas (id),
    constraint ck_jogo_itens_nivel check (nivel between 1 and 5)
);

create index ix_jogo_itens_vila on jogo_itens (vila_id);

create table jogo_unidades (
    id                 bigint generated always as identity,
    vila_id            bigint not null,
    tipo               varchar(20) not null,
    arma_item_id       bigint not null,
    armadura_item_id   bigint not null,
    status             varchar(20) not null,
    criado_em          timestamptz not null,
    criado_por         varchar(150) not null,
    alterado_em        timestamptz not null,
    alterado_por       varchar(150) not null,
    constraint pk_jogo_unidades primary key (id),
    constraint fk_jogo_unidades_vila foreign key (vila_id) references jogo_vilas (id),
    constraint fk_jogo_unidades_arma_item foreign key (arma_item_id) references jogo_itens (id),
    constraint fk_jogo_unidades_armadura_item foreign key (armadura_item_id) references jogo_itens (id),
    constraint uk_jogo_unidades_arma_item unique (arma_item_id),
    constraint uk_jogo_unidades_armadura_item unique (armadura_item_id)
);

create index ix_jogo_unidades_vila on jogo_unidades (vila_id);

create table jogo_ordens (
    id                 bigint generated always as identity,
    vila_id            bigint not null,
    categoria          varchar(20) not null,
    alvo               varchar(30) not null,
    nivel              int null,
    quantidade         int not null default 1,
    arma_item_id       bigint null,
    armadura_item_id   bigint null,
    iniciada_em        timestamptz not null,
    conclui_em         timestamptz not null,
    criado_em          timestamptz not null,
    criado_por         varchar(150) not null,
    alterado_em        timestamptz not null,
    alterado_por       varchar(150) not null,
    constraint pk_jogo_ordens primary key (id),
    constraint fk_jogo_ordens_vila foreign key (vila_id) references jogo_vilas (id),
    constraint fk_jogo_ordens_arma_item foreign key (arma_item_id) references jogo_itens (id),
    constraint fk_jogo_ordens_armadura_item foreign key (armadura_item_id) references jogo_itens (id),
    constraint uk_jogo_ordens_vila_categoria unique (vila_id, categoria)
);

create index ix_jogo_ordens_vila on jogo_ordens (vila_id);

create table jogo_batalhas (
    id                bigint generated always as identity,
    vila_id           bigint not null,
    masmorra_nivel    int not null,
    status            varchar(20) not null,
    turno             int not null,
    estado            text not null,
    log               text not null,
    loot              text null,
    iniciada_em       timestamptz not null,
    finalizada_em     timestamptz null,
    version           bigint not null default 0,
    criado_em         timestamptz not null,
    criado_por        varchar(150) not null,
    alterado_em       timestamptz not null,
    alterado_por      varchar(150) not null,
    constraint pk_jogo_batalhas primary key (id),
    constraint fk_jogo_batalhas_vila foreign key (vila_id) references jogo_vilas (id)
);

create index ix_jogo_batalhas_vila on jogo_batalhas (vila_id);

create unique index ux_jogo_batalhas_vila_em_andamento on jogo_batalhas (vila_id) where status = 'EM_ANDAMENTO';
