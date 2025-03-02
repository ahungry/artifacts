DROP TABLE maps;

CREATE TABLE maps (
  name text,
  skin text,
  x int,
  y int,
  content_type text,
  content_code text,
  UNIQUE (x, y) ON CONFLICT REPLACE
);

INSERT INTO maps (name, skin, x, y, content_type, content_code)
VALUES ('City', 'forest_chicken1', 0, 1, 'monster', 'chicken');

.mode table

select * from maps;

select distinct(content_type) from maps;

select * from maps where content_type = 'resource';

select * from maps where content_type = 'workshop';

select * from maps where content_type = 'npc';

select distinct(type) from items;

select i.code, c.skill from inventory i
left join items it on i.code = it.code
left join crafts c on i.code = c.code
where i.name = 'ahungry'
and it.type IN ('weapon', 'boots', 'helmet', 'shield', 'leg_armor', 'body_armor')
and i.code <> ''
and i.code not in (select distinct(material_code) from crafts);
