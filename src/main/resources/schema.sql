--https://spring.io/guides/gs/accessing-data-r2dbc/
--서버가 구동될 때 마다 schema.sql 파일을 읽어와서 실행하게 된다.

DROP TABLE IF EXISTS customer;--table이 있으면 drop하고 다시 생성하도록
CREATE TABLE customer (id SERIAL PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255));