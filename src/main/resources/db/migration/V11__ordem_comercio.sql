-- Ordens de compra e venda no Mercado (limite de volume por turno e histórico).
create table ordem_comercio (
    id              bigint generated always as identity,
    vila_id         bigint not null,
    turno           int not null,
    tipo            varchar(10) not null,
    recurso         varchar(30) not null,
    quantidade      int not null,
    preco_unitario  numeric(12,2) not null,
    ouro_total      numeric(14,2) not null,
    criado_em       timestamptz not null default now(),
    constraint pk_ordem_comercio primary key (id),
    constraint fk_ordem_comercio_vila foreign key (vila_id) references vila (id),
    constraint ck_ordem_comercio_tipo check (tipo in ('COMPRA', 'VENDA')),
    constraint ck_ordem_comercio_quantidade check (quantidade > 0)
);

create index ix_ordem_comercio_vila_turno on ordem_comercio (vila_id, turno);
