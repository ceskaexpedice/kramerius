---------------------------
--        CLEANUP        --
---------------------------

--triggers, table
DROP TRIGGER IF EXISTS update_process_batch_on_process_insert on processes;
DROP TRIGGER IF EXISTS update_process_batch_on_process_update on processes;
DROP TRIGGER IF EXISTS update_process_batch_on_process_delete on processes;
DROP FUNCTION IF EXISTS refresh_process_batch() CASCADE;
DROP TABLE IF EXISTS process_batch CASCADE;

--functions
DROP AGGREGATE IF EXISTS batch_state(integer) CASCADE;
DROP FUNCTION IF EXISTS update_batch_state(integer,integer) CASCADE;

--ordinary (live) view - just for testing
DROP VIEW IF EXISTS process_batch_not_precomputed;

---------------------------
--    NEW DEFINITIONS    --
---------------------------


--Agregacni funkce bude zjistovat stav davky procesu podle stavu vsech procesu v davce
--see https://github.com/ceskaexpedice/kramerius/blob/b7b173c3d664d4982483131ff6a547f49d96f47e/common/src/main/java/cz/incad/kramerius/processes/States.java

--batch stavy budou jen PLANNED, RUNNING, FAILED, FINISHED
--pro srozumitelnost je budu tady oznacovat BATCH_PLANNED, BATCH_RUNNING, BATCH_FAILED, BATCH_FINISHED
--vyhodnoceni bude nasledovne:

--zacnu na BATCH_UNDEFINED a iteruju pres procesy

--jsem BATCH_UNDEFINED(-1), vidim PLANNED(5) => BATCH_PLANNED(0)
--jsem BATCH_UNDEFINED(-1), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_UNDEFINED(-1), vidim FINISHED(2) => BATCH_FINISHED(2)
--jsem BATCH_UNDEFINED(-1), vidim NOT_RUNNING(0)/FAILED(3)/WARNING(9) => BATCH_FAILED(3)
--jsem BATCH_UNDEFINED(-1), vidim KILLED(4) => BATCH_KILLED(4)

--jsem BATCH_PLANNED(0), vidim PLANNED(5) => BATCH_PLANNED(0)
--jsem BATCH_PLANNED(0), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_PLANNED(0), vidim FINISHED(2) => BATCH_RUNNING(1)
--jsem BATCH_PLANNED(0), vidim NOT_RUNNING(0)/FAILED(3)/WARNING(9) => BATCH_RUNNING(1)
--jsem BATCH_PLANNED(0), vidim KILLED(4) => BATCH_RUNNING(1)

--jsem BATCH_RUNNING(1), vidim PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim FINISHED(2) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim NOT_RUNNING(0)/FAILED(3)/WARNING(9) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim KILLED(4) => BATCH_KILLED(4)

--jsem BATCH_FINISHED(2), vidim PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_FINISHED(2), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_FINISHED(2), vidim FINISHED(2) => BATCH_FINISHED(2)
--jsem BATCH_FINISHED(2), vidim NOT_RUNNING(0)/FAILED(3)/WARNING(9) => BATCH_FAILED(3)
--jsem BATCH_FINISHED(2), vidim KILLED(4) => BATCH_KILLED(4)

--jsem BATCH_FAILED(3), vidim PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_FAILED(3), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_FAILED(3), vidim FINISHED(2) => BATCH_FAILED(3)
--jsem BATCH_FAILED(3), vidim NOT_RUNNING(0)/FAILED(3)/WARNING(9) => BATCH_FAILED(3)
--jsem BATCH_FAILED(3), vidim KILLED(4) => BATCH_KILLED(4)

--jsem BATCH_KILLED(4), vidim PLANNED(5) => BATCH_KILLED(4)
--jsem BATCH_KILLED(4), vidim RUNNING(1) => BATCH_KILLED(4)
--jsem BATCH_KILLED(4), vidim FINISHED(2) => BATCH_KILLED(4)
--jsem BATCH_KILLED(4), vidim NOT_RUNNING(0)/FAILED(3)/WARNING(9) => BATCH_KILLED(4)
--jsem BATCH_KILLED(4), vidim KILLED(4) => BATCH_KILLED(4)

--jinak BATCH_FAILED(3)

--first param is current (batch) state, secon param is process state
CREATE OR REPLACE FUNCTION update_batch_state(integer, integer) RETURNS integer AS '
  DECLARE r int;
  BEGIN
    IF $1 = -1 THEN -- BATCH_UNDEFINED
           IF $2 = 5 THEN RETURN 0;
        ELSIF $2 = 1 THEN RETURN 1;
        ELSIF $2 = 2 THEN RETURN 2;
        ELSIF $2 = 0 THEN RETURN 3;
        ELSIF $2 = 3 THEN RETURN 3;
        ELSIF $2 = 9 THEN RETURN 5;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    ELSEIF $1 = 0 THEN -- BATCH_PLANNED
           IF $2 = 5 THEN RETURN 1;
         ELSIF $2 = 1 THEN RETURN 1;
         ELSIF $2 = 2 THEN RETURN 1;
         ELSIF $2 = 0 THEN RETURN 1;
         ELSIF $2 = 3 THEN RETURN 1;
         ELSIF $2 = 9 THEN RETURN 5;
         ELSIF $2 = 4 THEN RETURN 1;
         END IF;
    ELSIF $1 = 1 THEN -- BATCH_RUNNING
           IF $2 = 5 THEN RETURN 1;
        ELSIF $2 = 1 THEN RETURN 1;
        ELSIF $2 = 2 THEN RETURN 1;
        ELSIF $2 = 0 THEN RETURN 1;
        ELSIF $2 = 3 THEN RETURN 1;
        ELSIF $2 = 9 THEN RETURN 5;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    ELSIF $1 = 2 THEN -- BATCH_FINISHED
           IF $2 = 5 THEN RETURN 1;
        ELSIF $2 = 1 THEN RETURN 1;
        ELSIF $2 = 2 THEN RETURN 2;
        ELSIF $2 = 0 THEN RETURN 3;
        ELSIF $2 = 3 THEN RETURN 3;
        ELSIF $2 = 9 THEN RETURN 5;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    ELSIF $1 = 3 THEN -- BATCH_FAILED
           IF $2 = 5 THEN RETURN 1;
        ELSIF $2 = 1 THEN RETURN 1;
        ELSIF $2 = 2 THEN RETURN 3;
        ELSIF $2 = 0 THEN RETURN 3;
        ELSIF $2 = 3 THEN RETURN 3;
        ELSIF $2 = 9 THEN RETURN 3;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    ELSIF $1 = 4 THEN -- BATCH_KILLED
        RETURN 4;
    ELSIF $1 = 5 THEN -- BATCH_WARNING
           IF $2 = 5 THEN RETURN 5;
        ELSIF $2 = 1 THEN RETURN 5;
        ELSIF $2 = 2 THEN RETURN 5;
        ELSIF $2 = 0 THEN RETURN 5;
        ELSIF $2 = 3 THEN RETURN 3;
        ELSIF $2 = 9 THEN RETURN 5;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    END IF;
    RETURN 3;
  END;
' LANGUAGE plpgsql;

--samotna agregacni funkce, da dohromady rozumny stav batche ze stavu jednotlivych procesu v nem
CREATE AGGREGATE batch_state(integer)
(
    sfunc = update_batch_state,
    stype = integer,
    initcond = -1
);

--(ne-materialized verze) view pro batch procesu, jen pro testovani
CREATE VIEW process_batch_not_precomputed AS
SELECT
    processes.token AS batch_token,
    batch_state(processes.status) AS batch_state,
    count(*) AS process_count,
    min(processes.process_id) AS first_process_id,
    min(processes.status) AS first_process_state,
    min(processes.uuid) AS first_process_uuid,
    min(processes.defid) AS first_process_defid,
    min(processes.name) AS first_process_name,
    min(processes.planned) AS planned,
    min(processes.started) AS started,
    max(processes.finished) AS finished,
    min(processes.owner_id) as owner_id,
    min(processes.owner_name) as owner_name
  FROM
    processes
  GROUP BY
    processes.token
  ORDER BY
    first_process_id DESC;
--drop this view and enable only for testing
DROP VIEW IF EXISTS process_batch_not_precomputed;


--nova tabulka process_batch
CREATE TABLE process_batch (
    batch_token VARCHAR(255) NOT NULL,
    batch_state INT NOT NULL,
    process_count INT NOT NULL,
    first_process_id INT NOT NULL,
    first_process_state INT NOT NULL,
    first_process_uuid VARCHAR(255) NOT NULL,
    first_process_defid VARCHAR(255) NOT NULL,
    first_process_name VARCHAR, --v processes.name muze byt null
    planned TIMESTAMP,
    started TIMESTAMP,
    finished TIMESTAMP,
    owner_id VARCHAR,
    owner_name VARCHAR,
    PRIMARY KEY (batch_token)
);

--uvodni inicializace process_batch
INSERT INTO process_batch (
SELECT
    processes.token AS batch_token,
    batch_state(processes.status) AS batch_state,
    count(*) AS process_count,
    min(processes.process_id) AS first_process_id,
    min(processes.status) AS first_process_state,
    min(processes.uuid) AS first_process_uuid,
    min(processes.defid) AS first_process_defid,
    min(processes.name) AS first_process_name,
    min(processes.planned) AS planned,
    min(processes.started) AS started,
    max(processes.finished) AS finished,
    min(processes.owner_id) as owner_id,
    min(processes.owner_name) as owner_name
  FROM
    processes
  GROUP BY
    processes.token
  ORDER BY
    first_process_id DESC
);

--funkce pro aktualizaci konkretniho radku v process_batch
CREATE OR REPLACE FUNCTION refresh_process_batch() RETURNS TRIGGER AS
$BODY$
  DECLARE
      rec record;
  BEGIN
    CASE TG_OP
      WHEN 'INSERT' THEN
          rec = NEW;
      WHEN 'UPDATE' THEN
          rec = NEW;
      WHEN 'DELETE' THEN
          rec = OLD;
    END CASE;
    --RAISE NOTICE 'refresh_process_batch(), rec.process_id=%', rec.process_id;
    DELETE FROM process_batch
           WHERE batch_token = rec.token;
    --RAISE NOTICE 'deleted, now inserting';
    INSERT into process_batch
    SELECT
        processes.token AS batch_token,
        batch_state(processes.status) AS batch_state,
        count(*) AS process_count,
        min(processes.process_id) AS first_process_id,
        min(processes.status) AS first_process_state,
        min(processes.uuid) AS first_process_uuid,
        min(processes.defid) AS first_process_defid,
        min(processes.name) AS first_process_name,
        min(processes.planned) AS planned,
        min(processes.started) AS started,
        max(processes.finished) AS finished,
        min(processes.owner_id) AS owner_id,
        min(processes.owner_name) AS owner_name
      FROM
        processes
      WHERE
        processes.token=rec.token
      GROUP BY
        processes.token
      ORDER BY
        first_process_id DESC
    ;
    --RAISE NOTICE 'inserted';
    RETURN NULL;
  END;
$BODY$
LANGUAGE plpgsql;

--trigger on insert
CREATE TRIGGER update_process_batch_on_process_insert
AFTER INSERT ON processes
    FOR EACH ROW
    EXECUTE PROCEDURE refresh_process_batch();

--trigger on update
CREATE TRIGGER update_process_batch_on_process_update
AFTER UPDATE ON processes
    FOR EACH ROW
    WHEN (OLD.* IS DISTINCT FROM NEW.*)
    EXECUTE PROCEDURE refresh_process_batch();

--trigger on delete
CREATE TRIGGER update_process_batch_on_process_delete
AFTER DELETE ON processes
    FOR EACH ROW
    EXECUTE PROCEDURE refresh_process_batch();

--TODO:
--mely by se pouklizet data, napr. odstranit batch_state z tabulky processes, taky pid (stejne nahrazen process_id)
--v produkci nechat jen jednu verzi view - materialized, nebo jinak predpocitana


