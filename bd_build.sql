create table Coordinates (
    id serial primary key,
    x bigint not null check ( x <= 955 ),
    y integer not null check ( y <= 207 )
);

create table Color (
  id serial primary key,
  color varchar(20) not null
);

create table Person (
  id serial primary key,
  name varchar not null check ( name not like '' ),
  birthday timestamp,
  height numeric(1000, 3) not null check ( height > 0 ),
  passportID varchar(38) not null check ( length(passportID) >= 4 ),
  heirColor integer references Color on delete set null
);

create table Dragon (
    id serial primary key,
    name varchar not null check ( name not like '' ),
    coordinates integer references Coordinates on delete set default default 0 not null,
    creationDate timestamp not null default localtimestamp,
    age bigint check ( age > 0),
    wingspan integer check ( wingspan > 0),
    weight numeric(1000, 3) check ( weight > 0) not null,
    color integer references Color on delete set default default 0 not null,
    killer integer references Person on delete set null
);

create table users (
    id serial primary key,
    login varchar(50) not null check ( login not like '' ) unique ,
    password varchar(128) not null,
    salt varchar(32) not null
);

create table users_dragon (
    users integer references users not null,
    dragon integer references dragon not null,
    primary key (users, dragon)
);

insert into color (color)
values ('RED'),
       ('YELLOW'),
       ('BROWN');

insert into person (name, birthday, height, passportID, heirColor)
values (
        'Egor',
        localtimestamp,
        12.532,
        'ereteg241532',
        (select id from Color where color = 'YELLOW')
       );

insert into coordinates (x, y)
values (10, 12);

insert into dragon (name, coordinates, age, wingspan, weight, color, killer)
values (
        'Ilya',
        (select id from Coordinates where x = 10 and y = 12),
        123,
        12.45,
        14.6,
        (select id from Color where color = 'RED'),
        (select id from Person where name = 'Egor' and passportID = 'ereteg241532')
       );

SELECT d.id AS id, d.name AS name, cr.id coordinatesId, x, y, creationDate, age, wingspan, weight, cl1.color AS color, p.id killerId, p.name killerName, birthday, height, passportID, cl2.color AS heirColor
FROM dragon d
         JOIN coordinates cr on cr.id = d.coordinates
         JOIN color cl1 on cl1.id = d.color
         LEFT JOIN person p on p.id = d.killer
         LEFT JOIN color cl2 on p.heirColor = cl2.id
ORDER BY age, d.name;

UPDATE dragon
SET name = ?, age = ?,wingspan = ?,
    weight = ?, color = (SELECT id FROM color WHERE Color.color = ?)
WHERE id = ?;

UPDATE coordinates
SET x = ?, y = ?
WHERE id = ?;

UPDATE person
SET name = ?, birthday = ?, height = ?,
    passportID = ?, heirColor = (SELECT id FROM color WHERE Color.color = ?)
WHERE id = ?;

SELECT coordinates, killer
FROM dragon
WHERE id = ?;

DELETE FROM person
WHERE id = ?;

DELETE FROM color cl1 USING color cl2
WHERE cl1.id > cl2.id AND cl1.color = cl2.color;