/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.Kramerius.views;

import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.States;

public class BatchProcessStateWrapper {

    private BatchStates bstate;
    private boolean selected = false;
    
    public BatchProcessStateWrapper(BatchStates bs) {
        super();
        this.bstate = bs;
    }

    public String getName() {
        return (this.bstate != null ? bstate.name() : "All");
    }
    
    public int getVal() {
        return (this.bstate != null ? bstate.getVal() : -1);
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public static List<BatchProcessStateWrapper> wrap(boolean withall, BatchStates... bstates) {
        List<BatchProcessStateWrapper> wrappers = new ArrayList<BatchProcessStateWrapper>();
        if (withall) {
            wrappers.add(new BatchProcessStateWrapper(null));
        }
        for (BatchStates bst : bstates) {
            wrappers.add(new BatchProcessStateWrapper(bst));
        }
        return wrappers;
    }
}
