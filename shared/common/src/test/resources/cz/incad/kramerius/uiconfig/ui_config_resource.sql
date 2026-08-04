CREATE TABLE ui_config_resource
(
    resource_key TEXT PRIMARY KEY,
    content_type TEXT  NOT NULL,
    content      BYTEA NOT NULL
);