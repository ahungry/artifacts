DROP TABLE crafts;

CREATE TABLE crafts (
  code text,
  skill text,
  level int,
  quantity int,
  material_code text,
  material_quantity int
);

.mode table

pragma table_info(crafts);

select code, type, subtype from crafts limit 10;

delete from crafts where 1=1;

select count(*) FROM crafts;

select * FROM crafts order by code;

select count(distinct(code)) FROM crafts order by code;

select * FROM crafts where material_code = 'copper' order by level, code;

select * FROM inventory;

select * FROM chars;
