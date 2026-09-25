-- Seed do perfil administrativo inicial.

insert into perfis (nome, descricao, criado_em, criado_por, alterado_em, alterado_por)
values ('ADMIN', 'Administrador do sistema', now(), 'sistema', now(), 'sistema');
