DROP TABLE chars;

CREATE TABLE chars (
  name text PRIMARY KEY ON CONFLICT REPLACE,
  level int,
  attack_fire int,
  attack_earth int,
  attack_water int,
  attack_air int,
  max_hp int,
  hp int,
  x int,
  y int,
  cooldown_expiration text,
  cooldown int,
  xp int,
  max_xp int,
  gold int
);

pragma table_info(chars);

-- INSERT INTO chars (name, skin, x, y, content_type, content_code)
-- VALUES ('City', 'forest_chicken1', 0, 1, 'monster', 'chicken');

.mode table

select * from chars;
