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

select
case when
  coalesce(
    null
  , 0) >
  coalesce(
    (select (iiii.quality - 1) from chars iiic left join items iiii on (iiii.code=iiic.ring2_slot))
  , 0)
then 'ring2' else 'ring1'
end;
