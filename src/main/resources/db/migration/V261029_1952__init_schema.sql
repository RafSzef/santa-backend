CREATE TABLE pages (
    code VARCHAR(64) NOT NULL PRIMARY KEY,
    password_hash VARCHAR(255),
    updated_at TIMESTAMP
);

CREATE TABLE letters (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(32) NOT NULL DEFAULT 'ADULT',
    name VARCHAR(255) NOT NULL,
    description TEXT,
    page_code VARCHAR(64),
    CONSTRAINT fk_letters_page FOREIGN KEY (page_code) REFERENCES pages(code) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_letters_page_code ON letters(page_code);

CREATE TABLE gifts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    link VARCHAR(2048),
    is_selected BOOLEAN NOT NULL DEFAULT FALSE,
    is_claimed BOOLEAN NOT NULL DEFAULT FALSE,
    letter_id BIGINT,
    CONSTRAINT fk_gifts_letter FOREIGN KEY (letter_id) REFERENCES letters(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_gifts_letter_id ON gifts(letter_id);
