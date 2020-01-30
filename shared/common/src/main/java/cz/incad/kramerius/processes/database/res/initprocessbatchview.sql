
--Agregacni funkce bude zjistovat stav davky procesu podle stavu potomku
--see https://github.com/ceskaexpedice/kramerius/blob/b7b173c3d664d4982483131ff6a547f49d96f47e/common/src/main/java/cz/incad/kramerius/processes/States.java

--batch stavy budou jen PLANNED, RUNNING, FAILED, FINISHED
--pro srozumitelnost je budu tady oznacovat BATCH_PLANNED, BATCH_RUNNING, BATCH_FAILED, BATCH_FINISHED
--vyhodnoceni bude nasledovne:

--zacnu na BATCH_PLANNED a iteruju pres procesy

--jsem BATCH_PLANNED(5), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_PLANNED(5)
--jsem BATCH_PLANNED(5), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_PLANNED(5), vidim FAILED(3)/WARNING(9)/KILLED(4) => BATCH_FAILED(3)
--jsem BATCH_PLANNED(5), vidim FINISHED(2) => BATCH_FINISHED(2)

--jsem BATCH_RUNNING(1), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim FAILED(3)/WARNING(9)/KILLED(4) => BATCH_RUNNING(1)
--jsem BATCH_RUNNING(1), vidim FINISHED(2) => BATCH_RUNNING(1)

--jsem BATCH_FAILED(3), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_FAILED(3), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_FAILED(3), vidim FAILED(3)/WARNING(9)/KILLED(4) => BATCH_FAILED(3)
--jsem BATCH_FAILED(3), vidim FINISHED(2) => BATCH_FAILED(3)

--jsem BATCH_FINISHED(2), vidim NOT_RUNNING(0)/PLANNED(5) => BATCH_RUNNING(1)
--jsem BATCH_FINISHED(2), vidim RUNNING(1) => BATCH_RUNNING(1)
--jsem BATCH_FINISHED(2), vidim FAILED(3)/WARNING(9)/KILLED(4) => BATCH_FAILED(3)
--jsem BATCH_FINISHED(2), vidim FINISHED(2) => BATCH_FINISHED(2)

--jinak FAILED(3)

DROP AGGREGATE IF EXISTS batch_state(integer) CASCADE;
DROP FUNCTION IF EXISTS update_batch_state(integer,integer);
DROP VIEW IF EXISTS process_batch;
DROP MATERIALIZED VIEW IF EXISTS process_batch_materialized;


--first param is current (batch) state, secon param is process state
CREATE OR REPLACE FUNCTION update_batch_state(integer, integer) RETURNS integer AS '
  DECLARE r int;
  BEGIN
    IF $1 = 5 THEN
        IF $2 = 0 THEN
            RETURN 5;
        ELSIF $2 = 5 THEN
            RETURN 5;
        ELSIF $2 = 1 THEN
            RETURN 1;
        ELSIF $2 = 3 THEN
            RETURN 3;
        ELSIF $2 = 9 THEN
            RETURN 3;
        ELSIF $2 = 4 THEN
            RETURN 3;
        ELSIF $2 = 2 THEN
            RETURN 2;
        END IF;
    ELSIF $1 = 1 THEN
        RETURN 1;
    ELSIF $1 = 3 THEN
        IF $2 = 0 THEN
            RETURN 1;
        ELSIF $2 = 5 THEN
            RETURN 1;
        ELSIF $2 = 1 THEN
            RETURN 1;
        ELSIF $2 = 3 THEN
            RETURN 3;
        ELSIF $2 = 9 THEN
            RETURN 3;
        ELSIF $2 = 4 THEN
            RETURN 3;
        ELSIF $2 = 2 THEN
            RETURN 3;
        END IF;
     ELSIF $1 = 2 THEN
        IF $2 = 0 THEN
            RETURN 1;
        ELSIF $2 = 5 THEN
            RETURN 1;
        ELSIF $2 = 1 THEN
            RETURN 1;
        ELSIF $2 = 3 THEN
            RETURN 3;
        ELSIF $2 = 9 THEN
            RETURN 3;
        ELSIF $2 = 4 THEN
            RETURN 3;
        ELSIF $2 = 2 THEN
            RETURN 2;
        END IF;
    END IF;
    RETURN 3;
    END;
'LANGUAGE plpgsql;

--samotna agregacni funkce, da dohromady rozumny stav batche ze stavu jednotlivych procesu v nem
CREATE AGGREGATE batch_state(integer)
(
    sfunc = update_batch_state,
    stype = integer,
    initcond = 5
);

--(ne-materialized verze) view pro batch procesu
--todo: ownerFirstname a ownerSurname nebude potreba, neni to na razeni a pro jednotlive procesy se to pak vezme joinem
CREATE VIEW process_batch AS
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
    min(processes.loginname) as ownerLogin,
    min(processes.firstname) as ownerFirstname,
    min(processes.surname) as ownerSurname
  FROM
    processes
  GROUP BY
    processes.token
  ORDER BY
    first_process_id DESC;

--materialized verze view pro batch procesu
--kvuli toho, aby cteni, listovani procesy nebylo pomale (kolem 3 sekund pro 125k procesu pro ne-materialized verzi)
--potom by se ale mela resit aktualizace view triggerem, pokud je pridan novy proces
--optimalizace: omezeni na situace, kdy uz existuje jiny proces se stejnym tokenem, ale musi byt jistota, ze logika nedoplnuje token az dodatecne
CREATE MATERIALIZED VIEW process_batch_materialized AS
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
    min(processes.loginname) as ownerLogin,
    min(processes.firstname) as ownerFirstname,
    min(processes.surname) as ownerSurname
  FROM
    processes
  GROUP BY
    processes.token
  ORDER BY
    first_process_id DESC;


--TODO:
--mely by se pouklizet data, napr. odstranit batch_state z tabulky processes, taky pid (stejne nahrazen process_id)
--v produkci nechat jen jednu verzi view, asi materialized

--TODO: tohle jen docasne pro testovani, odstranit
--GRANT ALL PRIVILEGES ON DATABASE kramerius4 TO readaccess;
--GRANT ALL PRIVILEGES ON TABLE process_batch TO readaccess;
--GRANT ALL PRIVILEGES ON TABLE process_batch_materialized TO readaccess;
