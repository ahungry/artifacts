DROP TABLE bank;

CREATE TABLE bank (
  code text PRIMARY KEY ON CONFLICT REPLACE,
  quantity int
);

.mode table

select * from bank;
