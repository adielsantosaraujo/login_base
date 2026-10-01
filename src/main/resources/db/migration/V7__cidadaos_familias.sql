-- Cidadãos, famílias e profissões; confirmação da população inicial da vila.

create table familia (
    id         bigint generated always as identity,
    vila_id    bigint not null,
    sobrenome  varchar(100) not null,
    casa_id    bigint null,
    constraint pk_familia primary key (id),
    constraint fk_familia_vila foreign key (vila_id) references vila (id),
    constraint fk_familia_casa foreign key (casa_id) references construcao (id)
);

create index ix_familia_vila on familia (vila_id);

create table cidadao (
    id                     bigint generated always as identity,
    vila_id                bigint not null,
    familia_id             bigint not null,
    nome                   varchar(100) not null,
    sexo                   char(1) not null,
    idade_meses            int not null default 0,
    vit                    int not null default 0,
    forca                  int not null default 0,
    vel                    int not null default 0,
    inteligencia           int not null default 0,
    car                    int not null default 0,
    pontos_car_pendentes   int not null default 0,
    pontos_prof_pendentes  int not null default 0,
    conjuge_id             bigint null,
    pai_id                 bigint null,
    mae_id                 bigint null,
    vivo                   boolean not null default true,
    estado                 varchar(20) not null default 'SAUDAVEL',
    ferido_ate_turno       int null,
    faminto_turnos         int not null default 0,
    gestacao_turnos        int null,
    construcao_id          bigint null,
    tropa_id               bigint null,
    xp_guerreiro           int not null default 0,
    constraint pk_cidadao primary key (id),
    constraint fk_cidadao_vila foreign key (vila_id) references vila (id),
    constraint fk_cidadao_familia foreign key (familia_id) references familia (id),
    constraint fk_cidadao_conjuge foreign key (conjuge_id) references cidadao (id),
    constraint fk_cidadao_pai foreign key (pai_id) references cidadao (id),
    constraint fk_cidadao_mae foreign key (mae_id) references cidadao (id),
    constraint fk_cidadao_construcao foreign key (construcao_id) references construcao (id),
    constraint ck_cidadao_sexo check (sexo in ('M', 'F')),
    constraint ck_cidadao_estado check (estado in ('SAUDAVEL', 'FERIDO'))
);

create index ix_cidadao_vila on cidadao (vila_id);
create index ix_cidadao_familia on cidadao (familia_id);
create index ix_cidadao_construcao on cidadao (construcao_id);

create table cidadao_profissao (
    cidadao_id          bigint not null,
    profissao           varchar(30) not null,
    pontos_base         int not null default 0,
    turnos_experiencia  int not null default 0,
    constraint pk_cidadao_profissao primary key (cidadao_id, profissao),
    constraint fk_cidadao_profissao_cidadao foreign key (cidadao_id) references cidadao (id)
);

alter table vila
    add constraint fk_vila_familia_lider foreign key (familia_lider_id) references familia (id);

alter table vila
    add column populacao_confirmada boolean not null default false;
