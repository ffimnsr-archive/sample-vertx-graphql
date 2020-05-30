DELIMITER //

CREATE OR REPLACE FUNCTION fn_set_timestamp_on_update()
	RETURNS trigger
AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
