DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'alerts' AND column_name = 'zscore'
  ) THEN
    ALTER TABLE alerts RENAME COLUMN zscore TO z_score;
  ELSIF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'alerts' AND column_name = 'z_score'
  ) THEN
    ALTER TABLE alerts ADD COLUMN z_score double precision;
  END IF;
END $$;
