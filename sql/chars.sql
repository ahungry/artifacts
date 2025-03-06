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

select min(1, 2, 3);

select * from items where code='iron_ring';

select * from items where code='copper_ring';

select min(ii.quality) from chars c
left join items ii on min(ii.code=c.ring1_slot, ii.code=c.ring2_slot)
where c.name = 'ahungry';

select i.quality, i.*, c.ring1_slot, c.ring2_slot from chars c
left join items i on (i.code=ring2_slot or i.code=ring1_slot)
where c.name = 'ahungry' order by i.quality asc limit 2;

select
  case when
      (select coalesce(iii.quality, 0) from chars iic left join items iii on (iii.code=iic.ring1_slot) where iic.name='ahungry')
      >
      (select coalesce(iiii.quality, 0) from chars iiic left join items iiii on (iiii.code=iiic.ring2_slot) where iiic.name='ahungry')
  then 'ring2' else 'ring1'
end;

select 1+1;

select coalesce(iii.quality) from chars iic left join items iii on (iii.code=iic.ring1_slot);

select iiii.quality from chars iiic left join items iiii on (iiii.code=iiic.ring2_slot);
