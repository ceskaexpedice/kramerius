--this script keeps data in table processes in columns owner_id and owner_name filled even for processes run in the past or scheduled recently through old admin interface

--cleanup
DROP TRIGGER IF EXISTS update_process_owner_name on processes;
DROP FUNCTION IF EXISTS update_process_owner();

--set owner_id and owner_name from loginname, firstname and surname for existing data
UPDATE processes SET owner_id = loginname where owner_id IS NULL;
UPDATE processes SET owner_name = firstname || ' ' || surname WHERE owner_name IS NULL;

--function to update owner_id and owner_name (if empty) from loginname, firstname, surname
CREATE OR REPLACE FUNCTION update_process_owner() RETURNS TRIGGER AS
$BODY$
  BEGIN
    --RAISE NOTICE 'update_process_owner(), NEW.owner_id=%', NEW.process_id;
    --set owner_id to loginname where empty
    UPDATE processes
        SET owner_id = loginname
        WHERE process_id = NEW.process_id AND owner_id IS NULL;
    --set owner_name to firstname + ' ' + surname where empty
    UPDATE processes
        SET owner_name = (firstname || ' ' || surname)
        WHERE process_id = NEW.process_id AND owner_name IS NULL;
    --pozor, tahle funkce (spoustena triggerem po zmene v tabulce processes) meni stejnou tabulku
    --proto je potreba kontrolovat sloupce na prazdnost, jinak se zacyklime
    RETURN NULL;
  END;
$BODY$
LANGUAGE plpgsql;

--trigger for setting owner_id and owner_name for processes scheduled old way (with loginname, firstname, surname)
CREATE TRIGGER update_process_owner_name
    AFTER INSERT OR UPDATE ON processes
    FOR EACH ROW
    WHEN (NEW.owner_name IS NULL OR NEW.owner_id IS NULL)
    EXECUTE PROCEDURE update_process_owner();
