/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.processes;


import java.util.List;

/**
 * Represents process grouping state
 */
public enum BatchStates {

    /**
     * Simple process. Not batch.
     */
    NO_BATCH(0),

    /**
     * Batch started (contains child processes and all of them are
     * PLANNED or RUNNING).
     */
    BATCH_STARTED(1),
    
    /**
     * Batch failed (some of child process FAILED)
     */
    BATCH_FAILED(2),

    /**
     * Batch failed (some of child process finished with state WARNING)
     */
    BATCH_WARNING(4),

    
    /**
     * Batch finished (all child processes finished with state FINISH)
     */
    BATCH_FINISHED(3);


    
    /**  load from value */
    public static BatchStates load(int v) {
        for (BatchStates st : BatchStates.values()) {
            if (st.getVal() == v)
                return st;
        }
        return null;
    }

    
    private int val;

    private BatchStates(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
    

    /**
     * Calculate master batch process state
     * @param childStates Child process states
     * @return
     */
    public static BatchStates calculateBatchState(List<States> childStates) {
        // ve stavu planned nebo running
        if (States.one(childStates, States.FAILED, States.KILLED)) {
            return BATCH_FAILED;
        } else if (States.one(childStates,States.WARNING)) {
            return BATCH_WARNING;
        } else {
            if (States.one(childStates, States.PLANNED, States.RUNNING)) {
                return BATCH_STARTED;
            }
            return BATCH_FINISHED;
        }
    }

    /**
     * Returns true if one of given childStates contains any expecting state
     * @param childStates Child states
     * @param exp Expecting states
     * @return
     */
    public static boolean one(List<BatchStates> childStates, BatchStates... exp) {
        for (BatchStates st : childStates) {
            if (expect(st, exp))
                return true;
        }
        return false;
    }

    /**
     * Returns true if all of given child states contains any expecting state
     * @param childStates Child states
     * @param exp Execting state
     * @return
     */
    public static boolean all(List<BatchStates> childStates, BatchStates... exp) {
        for (BatchStates st : childStates) {
            if (!expect(st, exp))
                return false;
        }
        return true;
    }

    /**
     * Returns true if given real state is one of expecting state
     * @param real Real state
     * @param expected Expecting states
     * @return
     */
    public static boolean expect(BatchStates real, BatchStates... expected) {
        for (BatchStates exp : expected) {
            if (real.equals(exp))
                return true;
        }
        return false;
    }


    public static boolean notRunningBatch(BatchStates real) {
        return expect(real, NO_BATCH, BATCH_FINISHED, BATCH_FAILED);
    }
}
