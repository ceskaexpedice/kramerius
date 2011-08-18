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

    public boolean contains(String node) {
        return Arrays.asList(this.pathFromRootToLeaf).contains(node);
    }
    
    
    public String getNodeFromRootToLeaf(int index) {
        return getPathFromRootToLeaf()[index];
    }
    
    public String getNodeFromLeafToRoot(int index) {
        return getPathFromLeafToRoot()[index];
    }

    protected String[] cutHeadInternal(int indexFrom) {
        String[] path = getPathFromRootToLeaf();
        int nlength = path.length - (indexFrom+1);
        if (nlength < 0) throw new ArrayIndexOutOfBoundsException(nlength);
        String[] subpath = new String[nlength];
        System.arraycopy(path, indexFrom+1, subpath, 0, subpath.length);
        return subpath;
    }

    protected String[] cutTailInternal(int indexFrom) {
        String[] path = getPathFromRootToLeaf();
        int nlength = path.length - (indexFrom+1);
        if (nlength < 0) throw new ArrayIndexOutOfBoundsException(nlength);
        String[] subpath = new String[nlength];
        System.arraycopy(path, 0, subpath, 0, subpath.length);
        return subpath;
    }

    
    public abstract AbstractObjectPath cutHead(int indexFrom);

    
    public abstract AbstractObjectPath cutTail(int indexFrom);
    
    
    public String[] getPathFromRootToLeaf() {
        String[] array = Arrays.asList(this.pathFromRootToLeaf).toArray(new String[this.pathFromRootToLeaf.length]);
        return array;
    }

    public String[] getPathFromLeafToRoot() {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(this.pathFromRootToLeaf));
        Collections.reverse(list);
        return list.toArray(new String[this.pathFromRootToLeaf.length]);
    }

    public abstract AbstractObjectPath injectRepository();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(pathFromRootToLeaf);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractObjectPath other = (AbstractObjectPath) obj;
        if (!Arrays.equals(pathFromRootToLeaf, other.pathFromRootToLeaf))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return Arrays.asList(this.pathFromRootToLeaf).toString();
    }
    
}
