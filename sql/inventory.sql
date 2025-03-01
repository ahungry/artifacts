DROP TABLE inventory;

CREATE TABLE inventory (
  slot int PRIMARY KEY ON CONFLICT REPLACE,
  code text,
  quantity int
);

.mode table

select * from inventory;
