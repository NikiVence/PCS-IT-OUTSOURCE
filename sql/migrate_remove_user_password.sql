-- Выполнить один раз в существующей базе it_outsource.
ALTER TABLE users DROP COLUMN IF EXISTS password_hash;
