SELECT id
FROM "user"
WHERE login = ? AND
      password = ?;

SELECT salt
FROM "user"
WHERE login = ?;


SELECT id
FROM "user"
WHERE login = ?;

INSERT INTO "user" (login, password, salt)
VALUES (?, ?, ?);

INSERT INTO user_dragon ("user", dragon)
VALUES (?, ?);