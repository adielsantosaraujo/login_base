-- Esquema de controle de acesso: usuários, perfis, permissões, vínculos com vigência
-- e registro de sessões. Todas as tabelas têm auditoria (criado_em/criado_por/alterado_em/alterado_por),
-- preenchida pela aplicação (Spring Data JPA Auditing) ou pelas migrações de seed.

create table usuarios (
    id             bigint generated always as identity,
    nome           varchar(150) not null,
    email          varchar(150) not null,
    senha          varchar(255) not null,
    celular        varchar(11) null,
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_usuarios primary key (id),
    constraint uk_usuarios_celular unique (celular),
    constraint ck_usuarios_celular check (celular ~ '^[0-9]{11}$')
);

create unique index ux_usuarios_email_lower on usuarios (lower(email));

create table perfis (
    id             bigint generated always as identity,
    nome           varchar(100) not null,
    descricao      varchar(255),
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_perfis primary key (id),
    constraint uk_perfis_nome unique (nome)
);

create table permissoes (
    id             bigint generated always as identity,
    nome           varchar(100) not null,
    descricao      varchar(255),
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_permissoes primary key (id),
    constraint uk_permissoes_nome unique (nome)
);

create table usuario_rel_perfis (
    id             bigint generated always as identity,
    usuario_id     bigint not null,
    perfil_id      bigint not null,
    data_inicial   date not null,
    data_final     date,
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_usuario_rel_perfis primary key (id),
    constraint fk_usuario_rel_perfis_usuario foreign key (usuario_id) references usuarios (id),
    constraint fk_usuario_rel_perfis_perfil foreign key (perfil_id) references perfis (id),
    constraint ck_usuario_rel_perfis_vigencia check (data_final is null or data_final >= data_inicial)
);

create index ix_usuario_rel_perfis_usuario on usuario_rel_perfis (usuario_id);

create table perfis_rel_permissoes (
    id             bigint generated always as identity,
    perfil_id      bigint not null,
    permissao_id   bigint not null,
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_perfis_rel_permissoes primary key (id),
    constraint fk_perfis_rel_permissoes_perfil foreign key (perfil_id) references perfis (id),
    constraint fk_perfis_rel_permissoes_permissao foreign key (permissao_id) references permissoes (id),
    constraint uk_perfis_rel_permissoes unique (perfil_id, permissao_id)
);

create table sessoes (
    id             bigint generated always as identity,
    usuario_id     bigint not null,
    data_inicio    timestamptz not null,
    data_fim       timestamptz,
    token          varchar(64) not null,
    ip             varchar(45),
    dispositivo    varchar(500),
    criado_em      timestamptz not null,
    criado_por     varchar(150) not null,
    alterado_em    timestamptz not null,
    alterado_por   varchar(150) not null,
    constraint pk_sessoes primary key (id),
    constraint fk_sessoes_usuario foreign key (usuario_id) references usuarios (id),
    constraint uk_sessoes_token unique (token)
);

create index ix_sessoes_usuario on sessoes (usuario_id);
