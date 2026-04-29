-- pgvector 익스텐션 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- News 테이블 임베딩 인덱스 (HNSW)
CREATE INDEX IF NOT EXISTS idx_news_embedding
ON news USING hnsw (embedding vector_cosine_ops);

-- Keyword 테이블 임베딩 인덱스
CREATE INDEX IF NOT EXISTS idx_keyword_embedding
ON keywords USING hnsw (embedding vector_cosine_ops);

-- User 테이블 임베딩 인덱스
CREATE INDEX IF NOT EXISTS idx_user_embedding
ON users USING hnsw (embedding vector_cosine_ops);