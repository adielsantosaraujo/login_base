-- Batalhas de expedição (gravação do log e do resultado). masmorra_id sem FK (masmorras só na Fase 5).

create table batalha (
    id              bigint generated always as identity,
    vila_id         bigint not null,
    tropa_id        bigint null,
    tropa_nome      varchar(100) not null,
    masmorra_id     bigint null,
    masmorra_nivel  int not null,
    regiao_indice   int not null,
    turno           int not null,
    semente         bigint not null,
    resultado       varchar(20) not null,
    rodadas         int not null,
    log             jsonb not null,
    recompensas     jsonb null,
    criado_em       timestamptz not null default now(),
    constraint pk_batalha primary key (id),
    constraint fk_batalha_vila foreign key (vila_id) references vila (id),
    constraint fk_batalha_tropa foreign key (tropa_id) references tropa (id) on delete set null,
    constraint ck_batalha_resultado check (resultado in ('VITORIA', 'DERROTA'))
);

create index ix_batalha_vila_turno on batalha (vila_id, turno desc);
create index ix_batalha_tropa on batalha (tropa_id);

alter table tropa add column ultima_batalha_id bigint null;
alter table tropa add constraint fk_tropa_ultima_batalha foreign key (ultima_batalha_id) references batalha (id) on delete set null;
