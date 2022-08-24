INSERT INTO users(login) VALUES ('login1'), ('login2'), ('login3');

INSERT INTO communities(name, description, created_at)
VALUES ('name1', 'description1', '2003-01-23'),
       ('name2', 'description2', '2003-01-23'),
       ('name3', 'description3', '2003-01-23');

INSERT INTO community_users(community_id, user_id)
VALUES (1, 'login1'), (1, 'login2'), (2, 'login2'), (3, 'login3')
