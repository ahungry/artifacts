DROP TABLE resources;

CREATE TABLE resources (
  code text primary key ON CONFLICT REPLACE,
  skill text,
  level int
);

.mode table

select * from resources where level < 3;
