DROP TABLE chars;

delete from chars where 1=1;

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
  inventory_max_items int,
  inventory_count_items int,
  weaponcrafting_level int,
  weaponcrafting_xp int,
  weaponcrafting_max_xp int,
  gearcrafting_level int,
  gearcrafting_xp int,
  gearcrafting_max_xp int,
  jewelrycrafting_level int,
  jewelrycrafting_xp int,
  jewelrycrafting_max_xp int,
  cooking_level int,
  cooking_xp int,
  cooking_max_xp int,
  weapon_slot text,
  rune_slot text,
  shield_slot text,
  helmet_slot text,
  body_armor_slot text,
  leg_armor_slot text,
  boots_slot text,
  ring1_slot text,
  ring2_slot text,
  amulet_slot text,
  artifact1_slot text,
  artifact2_slot text,
  artifact3_slot text,
  utility1_slot text,
  utility2_slot text,
  bag_slot text,
  gold int
);

pragma table_info(chars);

-- INSERT INTO chars (name, skin, x, y, content_type, content_code)
-- VALUES ('City', 'forest_chicken1', 0, 1, 'monster', 'chicken');

.mode table

.mode csv

select name, level, cooldown_expiration from chars;

select name from chars;

select count(*) from chars;

select count(*) from chars where name = 'ahungry';
