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
  woodcutting_level int,
  woodcutting_xp int,
  woodcutting_max_xp int,
  fishing_level int,
  fishing_xp int,
  fishing_max_xp int,
  mining_level int,
  mining_xp int,
  mining_max_xp int,
  alchemy_level int,
  alchemy_xp int,
  alchemy_max_xp int,
  gold int
);

pragma table_info(chars);

-- INSERT INTO chars (name, skin, x, y, content_type, content_code)
-- VALUES ('City', 'forest_chicken1', 0, 1, 'monster', 'chicken');

.mode table

select * from chars;
