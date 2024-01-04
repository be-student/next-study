drop table if exists USERS;

CREATE TABLE USERS
(
    userId   varchar(12) NOT NULL,
    password varchar(12) NOT NULL,
    name     VARCHAR(20) NOT NULL,
    email    VARCHAR(50),

    PRIMARY KEY (userId)
);

INSERT INTO USERS
VALUES ('admin', 'password', '자바지기', 'admin@slipp.net');
