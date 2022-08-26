INSERT INTO users(login) VALUES ('login1'), ('login2'), ('login3');

INSERT INTO communities(name, description, created_at)
VALUES ('name1', 'description1', '2003-01-23'),
       ('name2', 'description2', '2003-01-23'),
       ('name3', 'description3', '2003-01-23');

INSERT INTO community_users(community_id, user_id)
VALUES (1, 'login1'), (1, 'login2'), (2, 'login2'), (3, 'login3');

INSERT INTO posts(message, created_at, user_login, community_id)
VALUES ('message1', '2003-01-23', 'login1', 1),
       ('message2', '2003-01-23', 'login1', 1),
       ('message3', '2003-01-23', 'login2', 2);

INSERT INTO comments(message, created_at, post_id, user_login)
VALUES ('message1', '2003-01-23', 1, 'login1'),
       ('message2', '2003-01-23', 1, 'login1'),
       ('message3', '2003-01-23', 2, 'login2');

INSERT INTO likes(type, created_at, user_login, post_id, comment_id)
VALUES ('POST_LIKE', '2003-01-23', 'login1', 1, NULL),
       ('POST_LIKE', '2003-01-23', 'login2', 1, NULL),
       ('POST_LIKE', '2003-01-23', 'login3', 2, NULL);
