CREATE TABLE repository (id BIGINT, name VARCHAR(255), selected BOOLEAN, favorite boolean, tutorials INTEGER NOT NULL, updatedate TIMESTAMP DEFAULT CURRENT_DATE, PRIMARY KEY (id))