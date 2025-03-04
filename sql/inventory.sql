DROP TABLE inventory;

CREATE TABLE inventory (
  name text,
  slot int,
  code text,
  quantity int,
  UNIQUE (name, slot) ON CONFLICT REPLACE
);

.mode table

select * from inventory where name='ahungry';

select * from bank;


select code, quantity from inventory where name='ahungry'
union
select code, quantity from bank;

-- See if we have enough reagents to craft something
select code, sum(quantity) from
(select code, quantity from inventory where name='ahungry'
union
select code, quantity from bank
)
where code <> '' and quantity > 0
group by code;

select * from inventory where name=? and code=? and quantity>=?
