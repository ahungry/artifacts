DROP TABLE inventory;

CREATE TABLE inventory (
  name text,
  slot int,
  code text,
  quantity int,
  UNIQUE (name, slot) ON CONFLICT REPLACE
);

.mode table

select * from inventory;
