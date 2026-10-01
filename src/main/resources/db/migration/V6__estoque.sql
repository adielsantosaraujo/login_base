-- Estoque de recursos por vila (uma linha por recurso).

create table estoque (
    id          bigint generated always as identity,
    vila_id     bigint not null,
    recurso     varchar(30) not null,
    quantidade  numeric(14,2) not null default 0,
    constraint pk_estoque primary key (id),
    constraint uk_estoque_vila_recurso unique (vila_id, recurso),
    constraint fk_estoque_vila foreign key (vila_id) references vila (id),
    constraint ck_estoque_quantidade check (quantidade >= 0)
);
