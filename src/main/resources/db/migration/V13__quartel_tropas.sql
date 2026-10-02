-- Tropas do quartel. Membro de tropa = cidadao.tropa_id (+ posicao_tropa); sem tabela de membros.

create table tropa (
    id               bigint generated always as identity,
    vila_id          bigint not null,
    quartel_id       bigint not null,
    nome             varchar(100) not null,
    estado           varchar(20) not null default 'AQUARTELADA',
    masmorra_id      bigint null,
    regiao_destino   int null,
    turnos_viagem    int null,
    turnos_restantes int null,
    criado_em        timestamptz not null default now(),
    constraint pk_tropa primary key (id),
    constraint fk_tropa_vila foreign key (vila_id) references vila (id),
    constraint fk_tropa_quartel foreign key (quartel_id) references construcao (id),
    constraint uk_tropa_vila_nome unique (vila_id, nome),
    constraint ck_tropa_estado check (estado in ('AQUARTELADA', 'EM_VIAGEM_IDA', 'EM_VIAGEM_VOLTA'))
);

create index ix_tropa_vila on tropa (vila_id);
create index ix_tropa_quartel on tropa (quartel_id);

alter table cidadao add column posicao_tropa varchar(20) null;
alter table cidadao add constraint ck_cidadao_posicao_tropa check (posicao_tropa in ('FRENTE', 'RETAGUARDA'));
alter table cidadao add constraint fk_cidadao_tropa foreign key (tropa_id) references tropa (id);
create index ix_cidadao_tropa on cidadao (tropa_id);

alter table cidadao alter column xp_guerreiro drop default;
alter table cidadao alter column xp_guerreiro type numeric(6,2) using xp_guerreiro::numeric(6,2);
alter table cidadao alter column xp_guerreiro set default 0;
