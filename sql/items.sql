DROP TABLE items;

CREATE TABLE items (
  code text PRIMARY KEY ON CONFLICT REPLACE,
  name text,
  level int,
  type text,
  subtype text,
  description text,
  tradeable bool,
  quality float
);

.mode table

pragma table_info(items);

select code, type, subtype from items limit 10;

select code, quality from items where quality > 0 order by quality desc;

select distinct(type) from items order by type;

select iii.quality from chars iic left join items iii on (iii.code=iic.ring1_slot);

select (iii.quality - 1) from chars iic left join items iii on (iii.code=iic.ring2_slot);

-- recyclables
select i.*, c.skill from inventory i
left join items it on i.code = it.code
inner join crafts c on i.code = c.code
where i.code = 'wolf_ears'
and it.type IN ('weapon', 'boots', 'helmet', 'shield', 'leg_armor', 'body_armor', 'ring', 'amulet')
and i.code <> ''
and i.code not in (select distinct(material_code) from crafts)
;
