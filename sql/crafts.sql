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

select * FROM crafts where material_code = 'green_slimeball' order by level, code;

select * FROM crafts
where code = 'copper_armor'
;

select * from inventory;

.help mode

.mode line

-- See what we can craft for gearcrafting at this level
select c.code, skill, c.level, material_code, material_quantity FROM crafts c
left join items i on c.code=i.code
where skill='gearcrafting'
and c.level = 10
;

select * FROM crafts c
left join items i on c.code=i.code
where type='amulet'
and c.level = 5
;

select * from crafts c
where c.code='wolf_ears';

.headings on

.mode columns

select distinct(i.code), i.quality, i.level FROM crafts c
left join items i on c.code=i.code
where type='body_armor'
order by quality asc
limit 30
;

select * from crafts c
where c.code = 'multislimes_sword'
;
