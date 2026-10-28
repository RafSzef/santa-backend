-- Migracja podbijajaca wersje Flyway zgodnie z zasada append-only
-- Zapewnia istnienie indekow na kluczach obcych bez naruszania istniejacych danych

CREATE INDEX IF NOT EXISTS idx_letters_page_code ON letters(page_code);
CREATE INDEX IF NOT EXISTS idx_gifts_letter_id ON gifts(letter_id);
