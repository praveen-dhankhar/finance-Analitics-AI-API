CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE embeddings (
    id UUID PRIMARY KEY,
    embedding vector(1536),
    text TEXT,
    metadata JSONB
);
