-- Níveis estendidos (change raise-building-max-level-100, decisão D9):
--   * nível de prédio: 0..100 (antes 0..5);
--   * posição de canteiro: 1..24 (24 = canteiros no nível 100 da fazenda);
--   * nível de item: 1..23 (23 = nível máximo forjável na forja nível 100).

alter table jogo_predios drop constraint ck_jogo_predios_nivel;
alter table jogo_predios add constraint ck_jogo_predios_nivel check (nivel between 0 and 100);

alter table jogo_canteiros drop constraint ck_jogo_canteiros_posicao;
alter table jogo_canteiros add constraint ck_jogo_canteiros_posicao check (posicao between 1 and 24);

alter table jogo_itens drop constraint ck_jogo_itens_nivel;
alter table jogo_itens add constraint ck_jogo_itens_nivel check (nivel between 1 and 23);
