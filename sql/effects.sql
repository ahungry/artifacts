DROP TABLE effects;

CREATE TABLE effects (
  code text PRIMARY KEY ON CONFLICT REPLACE,
  name text,
  description text,
  type text,
  subtype text
);

.mode table

pragma table_info(effects);

select code, description, type
from effects where type='equipment'
limit 3;

-- .mode foo
