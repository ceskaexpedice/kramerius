
-- as user kramerius:
CREATE TABLE IF NOT EXISTS folder (
   uuid VARCHAR UNIQUE NOT NULL,
   name VARCHAR NOT NULL,
   items_count INTEGER NOT NULL DEFAULT 0,
   updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
   PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS folder_item (
   item_id VARCHAR  NOT NULL,
   folder_uuid VARCHAR NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
   PRIMARY KEY (folder_uuid, item_id)
);

CREATE TABLE IF NOT EXISTS folder_user (
   folder_uuid VARCHAR NOT NULL,
   user_id VARCHAR NOT NULL,
   user_role VARCHAR NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
   PRIMARY KEY (folder_uuid, user_id)
);

CREATE OR REPLACE FUNCTION update_folder_items_count_and_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        -- Calculate the new items_count for the folder
        UPDATE folder
        SET items_count = (
            SELECT COUNT(*) FROM folder_item WHERE folder_uuid = NEW.folder_uuid
        ),
        updated_at = NOW()
        WHERE uuid = NEW.folder_uuid;
    END IF;

    RETURN NEW; 
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_folder_items_count_and_updated_at_trigger 
AFTER INSERT OR UPDATE OR DELETE ON folder_item 
FOR EACH ROW EXECUTE PROCEDURE update_folder_items_count_and_updated_at();


