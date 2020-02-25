---------------------------
--        CLEANUP        --
---------------------------

--triggers
DROP TRIGGER IF EXISTS possibly_delete_process_auth_token_on_process_state_change on processes;

--function
DROP FUNCTION IF EXISTS delete_process_auth_tokens_if_in_final_state(integer) CASCADE;

--table with auth tokens
DROP TABLE IF EXISTS process_auth_token CASCADE;

---------------------------
--    NEW DEFINITIONS    --
---------------------------

-- auth_token procesu slouzi k autorizaci procesu v procesnim API tehdy, kdyz proces planuje dalsi procesy
-- TODO: precejen je tam jen uuid a nekdo to muze hadat. Takze vyhledove spis nahradit tak, ze v tabulce bude symetricky sifrovaci klic, ten zaroven proces dostane pri svem spusteni
-- proces pak bude posilat:
-- this_process_id,nonce,encrypted(this_process_id,nonce)
-- nebo rovnou verzi s daty pro novy proces:
-- this_process_id,ownerId,ownerName,batchToken,nonce-time,encrypted(this_process_id,ownerId,ownerName,batchToken,nonce-time), zaroven nonce-time zabrani utoku prehranim
-- Vyssi bezpecnost ma smysl uz jen proto, ze by se stejnym zpousobem autentizace mely resit veci, jako zmena stavu procesu, coz ted dela servlet lr
CREATE TABLE process_auth_token (
    process_id INT REFERENCES processes(process_id),
    auth_token VARCHAR(255) NOT NULL,
    PRIMARY KEY(process_id)
);

--funkce, ktera maze zaznam v process_auth_token tehdy, kdyz process skoncil (nejak - FAILED, FINISHED, KILLED, WARNING)
CREATE OR REPLACE FUNCTION delete_process_auth_tokens_if_in_final_state() RETURNS TRIGGER AS
$BODY$
  BEGIN
    DELETE FROM process_auth_token
    WHERE process_id IN
    (
      SELECT process_id
      FROM processes
      WHERE
      (process_id = NEW.process_id) AND --todo: tady ma byt id procesu z NEW
      (
       status = 2 OR --FINISHED
       status = 3 OR --FAILED
       status = 4 OR --KILLED
       status = 9    --WARNING
      )
    )
    ;
    --RAISE NOTICE 'deleted';
    RETURN NULL;
  END;
$BODY$
LANGUAGE plpgsql;

-- trigger, ktery smaze token, jakmile je proces v nekterem koncnem stavu
-- pro kody procesu viz cz.incad.kramerius.processes.States
CREATE TRIGGER possibly_delete_process_auth_token_on_process_state_change
AFTER UPDATE ON processes
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE PROCEDURE delete_process_auth_tokens_if_in_final_state();




