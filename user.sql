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