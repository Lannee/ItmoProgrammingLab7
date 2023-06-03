SELECT
      id
FROM
      users
WHERE
      login = ?
      AND password = ?;

SELECT
      salt
FROM
      users
WHERE
      login = ?;

SELECT id
FROM users
WHERE login = ?;

INSERT INTO users (login, password, salt)
VALUES (?, ?, ?);

INSERT INTO users_dragon (users, dragon)
VALUES (?, ?);

SELECT id
FROM users
WHERE users.login = ?;