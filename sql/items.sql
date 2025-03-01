DROP TABLE items;

CREATE TABLE items (
  code text PRIMARY KEY ON CONFLICT REPLACE,
  name text,
  level int,
  type text,
  subtype text,
  description text,
  tradeable bool
);

.mode table

pragma table_info(items);

select code, type, subtype from items limit 10;
