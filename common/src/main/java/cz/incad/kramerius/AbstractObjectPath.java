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
package cz.incad.kramerius;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class AbstractObjectPath {

    protected String[] pathFromRootToLeaf;

    public AbstractObjectPath(String ... pathFromRootToLeaf) {
        this.pathFromRootToLeaf = pathFromRootToLeaf;
    }

    public int getLength() {
        return this.pathFromRootToLeaf.length;
    }

    public boolean isEmptyPath() {
        return this.pathFromRootToLeaf != null && this.pathFromRootToLeaf.length == 0;
    }

    public String getRoot() {
        if (!isEmptyPath()) {
            return this.pathFromRootToLeaf[0];
        } else {
            return null;
        }
    }

    public String getLeaf() {
        if (!isEmptyPath()) {
            return this.pathFromRootToLeaf[this.pathFromRootToLeaf.length-1];
        } else {
            return null;
        }
    }

    public String[] getPathFromRootToLeaf() {
        return Arrays.asList(this.pathFromRootToLeaf).toArray(new String[this.pathFromRootToLeaf.length]);
    }

    public String[] getPathFromLeafToRoot() {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(this.pathFromRootToLeaf));
        Collections.reverse(list);
        return list.toArray(new String[this.pathFromRootToLeaf.length]);
    }

    public abstract AbstractObjectPath injectRepository();
}
