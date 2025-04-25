CREATE TABLE query_cache (
                             id TEXT PRIMARY KEY,
                             url TEXT NOT NULL,
                             source_library TEXT,
                             text_data TEXT,
                             binary_data BYTEA,
                             mime_type TEXT NOT NULL,
                             pid TEXT,
                             user_identification TEXT,
                             created_at TIMESTAMP DEFAULT NOW()
);


CREATE INDEX idx_query_cache_url ON query_cache(url);
CREATE INDEX idx_query_cache_source ON query_cache(source_library);
CREATE INDEX idx_query_cache_created_at ON query_cache(created_at);
CREATE INDEX idx_query_cache_pid ON query_cache(pid);
CREATE INDEX idx_query_cache_user_identification ON query_cache(user_identification);