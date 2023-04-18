--Replacing process batch 
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
