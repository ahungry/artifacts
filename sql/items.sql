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
