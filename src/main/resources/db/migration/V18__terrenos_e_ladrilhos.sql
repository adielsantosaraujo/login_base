-- V18: percentuais de tipos de terreno por região e ladrilhos com terreno e bônus.
-- Banco limpo: os dados antigos (regiao_bonus, ladrilho_jazida) não são convertidos.
drop table regiao_bonus;
drop table ladrilho_jazida;

create table regiao_terreno (
    id         bigint generated always as identity,
    regiao_id  bigint      not null,
    terreno    varchar(20) not null,
    posicao    smallint    not null,
    percentual smallint    not null,
    constraint pk_regiao_terreno primary key (id),
    constraint fk_regiao_terreno_regiao foreign key (regiao_id) references regiao (id),
    constraint uk_regiao_terreno_terreno unique (regiao_id, terreno),
    constraint uk_regiao_terreno_posicao unique (regiao_id, posicao),
    constraint ck_regiao_terreno_posicao check (posicao between 1 and 3),
    constraint ck_regiao_terreno_percentual check ((posicao = 1 and percentual between 20 and 60)
        or (posicao = 2 and percentual between 20 and 70) or (posicao = 3 and percentual between 10 and 60)),
    constraint ck_regiao_terreno_terreno check (terreno in ('FLORESTA', 'BARREIRO', 'PLANTACOES', 'CRIACOES',
        'ROCHA', 'FERRO', 'CARVAO', 'SALINAS', 'ENXOFRE', 'MILITAR', 'INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO'))
);

create table ladrilho (
    id              bigint generated always as identity,
    regiao_id       bigint      not null,
    x               smallint    not null,
    y               smallint    not null,
    terreno         varchar(20) not null,
    bonus_base      smallint    not null,
    bonus_adjacente smallint    not null,
    constraint pk_ladrilho primary key (id),
    constraint fk_ladrilho_regiao foreign key (regiao_id) references regiao (id),
    constraint uk_ladrilho_posicao unique (regiao_id, x, y),
    constraint ck_ladrilho_x check (x between 0 and 9),
    constraint ck_ladrilho_y check (y between 0 and 9),
    constraint ck_ladrilho_terreno check (terreno in ('FLORESTA', 'BARREIRO', 'PLANTACOES', 'CRIACOES',
        'ROCHA', 'FERRO', 'CARVAO', 'SALINAS', 'ENXOFRE', 'MILITAR', 'INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO')),
    constraint ck_ladrilho_bonus_base check (bonus_base between 0 and 100),
    constraint ck_ladrilho_bonus_adjacente check (bonus_adjacente in (0, 25, 50, 75, 100))
);
