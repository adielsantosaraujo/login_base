-- Pedras de bônus obtidas nas masmorras. item_id nulo = no inventário; preenchido = engastada.
-- ON DELETE CASCADE: itens de guerreiros mortos são apagados e levam as pedras junto.

create table pedra (
    id         bigint generated always as identity,
    vila_id    bigint not null,
    qualidade  varchar(20) not null,
    bonus      jsonb not null default '[]',
    item_id    bigint null,
    criada_em  timestamptz not null default now(),
    constraint pk_pedra primary key (id),
    constraint fk_pedra_vila foreign key (vila_id) references vila (id),
    constraint fk_pedra_item foreign key (item_id) references item (id) on delete cascade,
    constraint ck_pedra_qualidade check (qualidade in ('SIMPLES', 'BOA', 'EXCELENTE', 'DIVINA'))
);

create index ix_pedra_vila_item on pedra (vila_id, item_id);
create index ix_pedra_item on pedra (item_id);
