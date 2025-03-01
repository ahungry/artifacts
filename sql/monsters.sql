DROP TABLE monsters;

CREATE TABLE monsters (
  code text primary key ON CONFLICT REPLACE,
  level int,
  hp int,
  attack_fire int,
  attack_earth int,
  attack_water int,
  attack_air int
);

INSERT INTO monsters (code, level, hp, attack_fire, attack_earth, attack_water, attack_air)
VALUES ('chicken', 1, 60, 0, 0, 4, 0);

.mode table

select * from monsters where level < 3;
