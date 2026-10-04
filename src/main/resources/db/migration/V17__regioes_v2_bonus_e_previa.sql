alter table regiao drop constraint ck_regiao_tipo;
alter table regiao add constraint ck_regiao_tipo
    check (tipo is null or tipo in ('FLORESTA', 'PLANICIE', 'URBANA', 'LITORAL', 'MONTANHA'));

create table regiao_bonus (
    id        bigint generated always as identity,
    regiao_id bigint      not null,
    bonus     varchar(20) not null,
    posicao   smallint    not null,
    valor     int         not null,
    constraint pk_regiao_bonus primary key (id),
    constraint fk_regiao_bonus_regiao foreign key (regiao_id) references regiao (id),
    constraint uk_regiao_bonus_bonus unique (regiao_id, bonus),
    constraint uk_regiao_bonus_posicao unique (regiao_id, posicao),
    constraint ck_regiao_bonus_posicao check (posicao between 1 and 3),
    constraint ck_regiao_bonus_valor check ((posicao = 1 and valor between 35 and 50)
        or (posicao = 2 and valor between 16 and 34) or (posicao = 3 and valor between 5 and 15)),
    constraint ck_regiao_bonus_nome check (bonus in ('FLORESTA', 'BARREIRO', 'PLANTACOES', 'CRIACOES', 'ROCHA',
        'FERRO', 'CARVAO', 'SALINAS', 'ENXOFRE', 'MILITAR', 'INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO'))
);
create index ix_regiao_bonus_regiao on regiao_bonus (regiao_id);

create table vila_previa (
    usuario_id bigint      not null,
    previa_id  uuid        not null,
    semente    bigint      not null,
    rodada     int         not null,
    criado_em  timestamptz not null default current_timestamp,
    constraint pk_vila_previa primary key (usuario_id),
    constraint uk_vila_previa_previa unique (previa_id),
    constraint fk_vila_previa_usuario foreign key (usuario_id) references usuarios (id),
    constraint ck_vila_previa_rodada check (rodada >= 1)
);
