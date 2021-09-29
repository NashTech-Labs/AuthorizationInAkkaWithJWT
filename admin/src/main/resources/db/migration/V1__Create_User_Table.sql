-- -----------------------------------------------------
-- schema `accounts`
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS template;

CREATE TABLE IF NOT EXISTS capstone.user_login (
userid VARCHAR(50),
email VARCHAR(50),
password VARCHAR(50),
user_type VARCHAR(50),
status BOOL,
last_login VARCHAR(260),
created_date VARCHAR(260),
PRIMARY KEY(email,user_type)
);


CREATE TABLE IF NOT EXISTS template.user_admin (
email VARCHAR(50) PRIMARY KEY,
password VARCHAR(50),
name VARCHAR(100),
user_type VARCHAR(50),
key_to_create VARCHAR(100)
);

--password: admin@admin
--key: authenticated_admin_key
insert into template.user_admin values('admin@admin.com','XuR2SZjp0jp8tSS/+BCc4A==','admin admin',
'admin','9RLv0ZULqbagNLYyAhUQS0gsHJ/6JQF+mWI2u5xiAZM=');

