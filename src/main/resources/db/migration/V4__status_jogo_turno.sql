-- Status do turno global: PROCESSANDO enquanto o agendador executa, CONCLUIDO ao final.

alter table jogo_turno
    add column status varchar(20) not null default 'CONCLUIDO';

alter table jogo_turno
    add constraint ck_jogo_turno_status check (status in ('PROCESSANDO', 'CONCLUIDO'));
