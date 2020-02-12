
--Agregacni funkce bude zjistovat stav davky procesu podle stavu potomku
--see https://github.com/ceskaexpedice/kramerius/blob/b7b173c3d664d4982483131ff6a547f49d96f47e/common/src/main/java/cz/incad/kramerius/processes/States.java

--batch stavy budou jen PLANNED, RUNNING, FAILED, FINISHED
--pro srozumitelnost je budu tady oznacovat BATCH_PLANNED, BATCH_RUNNING, BATCH_FAILED, BATCH_FINISHED
--vyhodnoceni bude nasledovne:

--zacnu na BATCH_PLANNED a iteruju pres procesy

--jsem BATCH_PLANNED(0), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_PLANNED(0)
--jsem BATCH_PLANNED(0), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_PLANNED(0), vidim FINISHED(2) => BATCH_FINISHED(2)
--jsem BATCH_PLANNED(0), vidim FAILED(3)/WARNING(9) => BATCH_FAILED(3)
--jsem BATCH_PLANNED(0), vidim KILLED(4) => BATCH_KILLED(4)

--jsem BATCH_RUNNING(1), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim FINISHED(2) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim FAILED(3)/WARNING(9) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim KILLED(4) => BATCH_KILLED(4)

--jsem BATCH_FINISHED(2), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_FINISHED(2), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_FINISHED(2), vidim FINISHED(2) => BATCH_FINISHED(2)
--jsem BATCH_FINISHED(2), vidim FAILED(3)/WARNING(9) => BATCH_FAILED(3)
--jsem BATCH_FINISHED(2), vidim KILLED(4) => BATCH_KILLED(4)

--jsem BATCH_FAILED(3), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_FAILED(3), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_FAILED(3), vidim FINISHED(2) => BATCH_FAILED(3)
--jsem BATCH_FAILED(3), vidim FAILED(3)/WARNING(9) => BATCH_FAILED(3)
--jsem BATCH_FAILED(3), vidim KILLED(4) => BATCH_KILLED(4)

--jsem BATCH_KILLED(4), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_KILLED(4)
--jsem BATCH_KILLED(4), vidim RUNNING(1) => BATCH_KILLED(4)
--jsem BATCH_KILLED(4), vidim FINISHED(2) => BATCH_KILLED(4)
--jsem BATCH_KILLED(4), vidim FAILED(3)/WARNING(9) => BATCH_KILLED(4)
--jsem BATCH_KILLED(4), vidim KILLED(4) => BATCH_KILLED(4)

--jinak BATCH_FAILED(3)

--functions
DROP AGGREGATE IF EXISTS batch_state(integer) CASCADE;
DROP FUNCTION IF EXISTS update_batch_state(integer,integer) CASCADE;

--ordinary (live) view - just for testing
DROP VIEW IF EXISTS process_batch_not_precomputed;

--materialized view
DROP TRIGGER IF EXISTS update_process_batch_on_process_state_change on processes;
DROP TRIGGER IF EXISTS update_process_batch_on_process_insert on processes;
DROP TRIGGER IF EXISTS update_process_batch_on_process_delete on processes;
DROP FUNCTION IF EXISTS refresh_process_batch();
DROP MATERIALIZED VIEW IF EXISTS process_batch;

--first param is current (batch) state, secon param is process state
CREATE OR REPLACE FUNCTION update_batch_state(integer, integer) RETURNS integer AS '
  DECLARE r int;
  BEGIN
    IF $1 = 0 THEN -- BATCH_PLANNED
           IF $2 = 0 THEN RETURN 0;
        ELSIF $2 = 5 THEN RETURN 0;
        ELSIF $2 = 1 THEN RETURN 1;
        ELSIF $2 = 2 THEN RETURN 2;
        ELSIF $2 = 3 THEN RETURN 3;
        ELSIF $2 = 9 THEN RETURN 3;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    ELSIF $1 = 1 THEN -- BATCH_RUNNING
           IF $2 = 0 THEN RETURN 1;
        ELSIF $2 = 5 THEN RETURN 1;
        ELSIF $2 = 1 THEN RETURN 1;
        ELSIF $2 = 2 THEN RETURN 1;
        ELSIF $2 = 3 THEN RETURN 1;
        ELSIF $2 = 9 THEN RETURN 1;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    ELSIF $1 = 2 THEN -- BATCH_FINISHED
           IF $2 = 0 THEN RETURN 1;
        ELSIF $2 = 5 THEN RETURN 1;
        ELSIF $2 = 1 THEN RETURN 1;
        ELSIF $2 = 2 THEN RETURN 2;
        ELSIF $2 = 3 THEN RETURN 3;
        ELSIF $2 = 9 THEN RETURN 3;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    ELSIF $1 = 3 THEN -- BATCH_FAILED
           IF $2 = 0 THEN RETURN 1;
        ELSIF $2 = 5 THEN RETURN 1;
        ELSIF $2 = 1 THEN RETURN 1;
        ELSIF $2 = 2 THEN RETURN 3;
        ELSIF $2 = 3 THEN RETURN 3;
        ELSIF $2 = 9 THEN RETURN 3;
        ELSIF $2 = 4 THEN RETURN 4;
        END IF;
    ELSIF $1 = 4 THEN -- BATCH_KILLED
        RETURN 4;
    END IF;
    RETURN 3;
  END;
' LANGUAGE plpgsql;

--samotna agregacni funkce, da dohromady rozumny stav batche ze stavu jednotlivych procesu v nem
CREATE AGGREGATE batch_state(integer)
(
    sfunc = update_batch_state,
    stype = integer,
    initcond = 0
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

--materialized verze view pro batch procesu
--kvuli toho, aby cteni, listovani procesy bylo pouzitelne (coz neni pri cekani kolem 3 sekund pro 125k procesu pro ne-materialized verzi)
CREATE MATERIALIZED VIEW process_batch AS
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


--funkce pro obnoveni materialized view
CREATE FUNCTION refresh_process_batch() RETURNS TRIGGER AS '
    BEGIN
        REFRESH MATERIALIZED VIEW process_batch;
        RETURN NULL;
    END;
' LANGUAGE plpgsql;


--trigger, ktery pri zmene stavu nektereho procesu prekeneruje cele materialized view process_batch
--TODO: jeste optimalizovat, pro 125k procesu to trva jednotky sekund a deje se to pri zmene stavu kazdeho procesu
--moznosti optimalizace: 1. omezit sloupce v process_batch jen na ty nezbytne pro razeni (nejspis jen mirne zrychleni), 2. misto materialized view to delat vlastni tabulkou a menit jen dotceny radek, ne vsechno
--TODO: neaktualizuje se po zmene date-time, coz vypada, ze se prakticky deje (neprve se zmeni stav a az v dalsi operaci timestamp)
CREATE TRIGGER update_process_batch_on_process_state_change
    AFTER UPDATE ON processes
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE PROCEDURE refresh_process_batch();

--podobne trigger po vlozeni procesu
CREATE TRIGGER update_process_batch_on_process_insert
    AFTER INSERT ON processes
    FOR STATEMENT
    EXECUTE PROCEDURE refresh_process_batch();

--podobne trigger po odstraneni procesu
CREATE TRIGGER update_process_batch_on_process_delete
    AFTER DELETE ON processes
    FOR STATEMENT
    EXECUTE PROCEDURE refresh_process_batch();


--TODO:
--mely by se pouklizet data, napr. odstranit batch_state z tabulky processes, taky pid (stejne nahrazen process_id)
--v produkci nechat jen jednu verzi view - materialized, nebo jinak predpocitana
