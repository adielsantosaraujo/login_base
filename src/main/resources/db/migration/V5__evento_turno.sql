-- Eventos gerados pelo processamento do turno por vila e controle de idempotência.

alter table vila add turno_processado int null;

create table evento_turno (
    id         bigint generated always as identity,
    vila_id    bigint not null,
    turno      int not null,
    tipo       varchar(40) not null,
    mensagem   varchar(500) null,
    dados      jsonb null,
    criado_em  timestamptz not null default now(),
    constraint pk_evento_turno primary key (id),
    constraint fk_evento_turno_vila foreign key (vila_id) references vila (id)
);

create index ix_evento_turno_vila_turno on evento_turno (vila_id, turno);
