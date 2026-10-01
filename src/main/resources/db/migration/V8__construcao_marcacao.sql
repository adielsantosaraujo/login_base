-- Ladrilhos marcados para coleta por prédios de coleta.

create table construcao_marcacao (
    id             bigint generated always as identity,
    vila_id        bigint not null,
    regiao_indice  int not null,
    x              int not null,
    y              int not null,
    construcao_id  bigint not null,
    constraint pk_construcao_marcacao primary key (id),
    constraint fk_construcao_marcacao_vila foreign key (vila_id) references vila (id),
    constraint fk_construcao_marcacao_construcao foreign key (construcao_id) references construcao (id) on delete cascade,
    constraint uk_construcao_marcacao_ladrilho unique (vila_id, regiao_indice, x, y)
);

create index ix_construcao_marcacao_construcao on construcao_marcacao (construcao_id);
