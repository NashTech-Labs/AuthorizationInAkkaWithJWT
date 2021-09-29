-- -----------------------------------------------------
-- schema `user_details`
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS accounts;

CREATE TABLE IF NOT EXISTS accounts.user_login (
userid VARCHAR(80),
email VARCHAR(50),
password VARCHAR(50),
user_type VARCHAR(50),
status BOOL,
last_login VARCHAR(260),
created_date VARCHAR(260),
PRIMARY KEY(email,user_type)
);


CREATE TABLE IF NOT EXISTS accounts.user_details (
userid VARCHAR(80),
email VARCHAR(50),
name VARCHAR(100),
user_type VARCHAR(50),
points INTEGER,
PRIMARY KEY(email,user_type),
KEY `fk_user_details` (email,user_type),
CONSTRAINT `fk_user_details`
FOREIGN KEY (email, user_type)
REFERENCES accounts.user_login (email, user_type)
ON DELETE CASCADE ON UPDATE CASCADE);

CREATE TABLE IF NOT EXISTS accounts.user_admin (
email VARCHAR(50) PRIMARY KEY,
password VARCHAR(50),
name VARCHAR(100),
user_type VARCHAR(50),
key_to_create VARCHAR(100)
);


CREATE TABLE IF NOT EXISTS accounts.message_logs (
message_id VARCHAR(150) PRIMARY KEY,
email VARCHAR(50),
user_type VARCHAR(50),
request_type VARCHAR(100),
request_at  VARCHAR(260),
status_code INTEGER,
message VARCHAR(200)
);

