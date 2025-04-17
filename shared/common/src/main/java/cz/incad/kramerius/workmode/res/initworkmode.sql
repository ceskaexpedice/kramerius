CREATE TABLE workmode (id VARCHAR PRIMARY KEY CHECK (id = 'singleton'), readOnly BOOLEAN NOT NULL);
