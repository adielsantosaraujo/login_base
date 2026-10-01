-- Tabelas base do jogo de vila: turno global, vila, regiões, jazidas e construções.

create table jogo_turno (
    numero         int not null,
    iniciado_em    timestamptz not null,
    concluido_em   timestamptz null,
    constraint pk_jogo_turno primary key (numero)
);

create table vila (
    id                bigint generated always as identity,
    usuario_id        bigint not null,
    nome              varchar(255),
    semente           bigint not null,
    turno_criacao     int not null,
    familia_lider_id  bigint,
    bem_alimentada    boolean not null default false,
    version           int not null default 0,
    constraint pk_vila primary key (id),
    constraint uk_vila_usuario unique (usuario_id),
    constraint fk_vila_usuario foreign key (usuario_id) references usuarios (id)
);

create table regiao (
    id               bigint generated always as identity,
    vila_id          bigint not null,
    indice           int not null,
    tipo             varchar(20) null,
    possuida         boolean not null default false,
    limpa_ate_turno  int null,
    constraint pk_regiao primary key (id),
    constraint fk_regiao_vila foreign key (vila_id) references vila (id),
    constraint uk_regiao_vila_indice unique (vila_id, indice),
    constraint ck_regiao_indice check (indice between 1 and 16),
    constraint ck_regiao_tipo check (tipo is null or tipo in ('RURAL', 'URBANA', 'COLETA'))
);

create table ladrilho_jazida (
    regiao_id  bigint not null,
    x          int not null,
    y          int not null,
    jazida     varchar(20) not null,
    constraint pk_ladrilho_jazida primary key (regiao_id, x, y),
    constraint fk_ladrilho_jazida_regiao foreign key (regiao_id) references regiao (id),
    constraint ck_ladrilho_jazida_x check (x between 0 and 9),
    constraint ck_ladrilho_jazida_y check (y between 0 and 9)
);

create table construcao (
    id             bigint generated always as identity,
    vila_id        bigint not null,
    tipo           varchar(50) not null,
    nivel          varchar(2) not null,
    regiao_indice  int not null,
    x              int not null,
    y              int not null,
    tamanho        int not null,
    estado         varchar(20) not null,
    po_total       int not null,
    po_atual       int not null,
    configuracao   jsonb null,
    criado_em      timestamptz not null default current_timestamp,
    atualizado_em  timestamptz not null default current_timestamp,
    constraint pk_construcao primary key (id),
    constraint fk_construcao_vila foreign key (vila_id) references vila (id),
    constraint ck_construcao_nivel check (nivel in ('N1', 'N2', 'N3')),
    constraint ck_construcao_regiao_indice check (regiao_indice between 1 and 16),
    constraint ck_construcao_tamanho check (tamanho in (1, 2, 3)),
    constraint ck_construcao_estado check (estado in ('EM_OBRA', 'ATIVA', 'EM_UPGRADE'))
);

create index ix_construcao_vila_regiao on construcao (vila_id, regiao_indice);
