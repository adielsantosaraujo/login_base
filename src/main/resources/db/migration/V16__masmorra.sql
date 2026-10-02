-- Masmorras da vila. Inativas ficam no historico; sem FK a partir de tropa/batalha (masmorra_id sem FK).

create table masmorra (
    id                  bigint generated always as identity,
    vila_id             bigint not null,
    regiao_indice       int not null,
    nivel               int not null,
    turno_surgimento    int not null,
    turnos_sem_ataque   int not null default 0,
    turno_ultimo_ataque int null,
    ativa               boolean not null default true,
    criado_em           timestamptz not null default now(),
    constraint pk_masmorra primary key (id),
    constraint fk_masmorra_vila foreign key (vila_id) references vila (id),
    constraint ck_masmorra_regiao check (regiao_indice between 1 and 16),
    constraint ck_masmorra_nivel check (nivel between 1 and 10)
);

create unique index ux_masmorra_vila_regiao_ativa on masmorra (vila_id, regiao_indice) where ativa;
create index ix_masmorra_vila_ativa on masmorra (vila_id, ativa);
