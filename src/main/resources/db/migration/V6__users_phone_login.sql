-- =====================================================================
-- V6__users_phone_login.sql  -  users can log in with their phone number (password or SMS code)
-- Phones are stored in one format, +85512345678 (see PhoneNumbers.normalize), and must be unique,
-- otherwise one number could open two accounts.
-- =====================================================================

-- 1. Same format as PhoneNumbers.normalize for the phones already saved:
--    drop spaces, dashes, dots and brackets; 00… → +…; 0… → +855…; 855… → +855…
UPDATE users SET phone = NULLIF(regexp_replace(phone, '[\s\-.()]', '', 'g'), '')
WHERE phone IS NOT NULL;

UPDATE users SET phone = CASE
        WHEN phone LIKE '00%'  THEN '+'    || substr(phone, 3)
        WHEN phone LIKE '0%'   THEN '+855' || substr(phone, 2)
        WHEN phone LIKE '855%' THEN '+'    || phone
        ELSE phone
    END
WHERE phone IS NOT NULL;

-- Anything that still isn't a phone number can't be used to log in: keep nothing rather than a wrong value
UPDATE users SET phone = NULL WHERE phone IS NOT NULL AND phone !~ '^\+[1-9][0-9]{7,14}$';

-- 2. Stop here if two accounts share a number, instead of picking one of them silently
DO $$
DECLARE duplicates TEXT;
BEGIN
    SELECT string_agg(phone || ' (users ' || ids || ')', ', ') INTO duplicates
    FROM (SELECT phone, string_agg(id::text, ', ' ORDER BY id) AS ids
          FROM users WHERE phone IS NOT NULL GROUP BY phone HAVING count(*) > 1) d;
    IF duplicates IS NOT NULL THEN
        RAISE EXCEPTION 'Phone numbers used by more than one user, fix them first: %', duplicates;
    END IF;
END $$;

-- 3. One account per phone number (users without a phone are not affected)
CREATE UNIQUE INDEX ux_users_phone ON users (phone) WHERE phone IS NOT NULL;
