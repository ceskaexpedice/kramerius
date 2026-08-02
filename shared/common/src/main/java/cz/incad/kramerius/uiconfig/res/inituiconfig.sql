CREATE TABLE ui_config(config_type TEXT PRIMARY KEY,config_json JSONB NOT NULL);
CREATE TABLE ui_config_resource (
                             resource_key TEXT PRIMARY KEY,
                             content_type TEXT NOT NULL,
                             content BYTEA NOT NULL
);