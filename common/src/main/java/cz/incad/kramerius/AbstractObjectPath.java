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

import cz.incad.kramerius.security.SpecialObjects;

/**
 * Represents path of objects or models
 * @author pavels
 */
public abstract class AbstractObjectPath {
    
    protected String[] pathFromRootToLeaf;

    /**
     * Default constructor
     * @param pathFromRootToLeaf Path from root to leaf
     */
    public AbstractObjectPath(String ... pathFromRootToLeaf) {
        this.pathFromRootToLeaf = pathFromRootToLeaf;
    }

    /**
     * Returns length of this path
     * @return
     */
    public int getLength() {
        return this.pathFromRootToLeaf.length;
    }

    /**
     * Returns true if this path is empty
     * @return true if this path is empty
     */
    public boolean isEmptyPath() {
        return this.pathFromRootToLeaf != null && this.pathFromRootToLeaf.length == 0;
    }

    /**
     * Returns root of the path
     * @return root of the path
     */
    public String getRoot() {
        if (!isEmptyPath()) {
            return this.pathFromRootToLeaf[0];
        } else {
            return null;
        }
    }

    /**
     * Returns leaf of the path
     * @return leaf of this path
     */
    public String getLeaf() {
        if (!isEmptyPath()) {
            return this.pathFromRootToLeaf[this.pathFromRootToLeaf.length-1];
        } else {
            return null;
        }
    }

    /**
     * Returns true if this path contains given node
     * @param node Checking node
     * @return true if this path contains given node
     */
    public boolean contains(String node) {
        return Arrays.asList(this.pathFromRootToLeaf).contains(node);
    }
    
    /**
     * Returns concrete node from this path. <br>
     * Inner exploring array is sorted from root to leaf 
     * @param index Index of node
     * @return node from this path.
     */
    public String getNodeFromRootToLeaf(int index) {
        return getPathFromRootToLeaf()[index];
    }
    
    
    /**
     * Returns concrete node from this path. <br>
     * Inner exploring array is sorted from leaf to root 
     * @param index Index of node
     * @return node from this path.
     */
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

    /**
     * Returns new instance of this path but without its head 
     * @param indexFrom Cut index
     * @return new instance of this path
     */
    public abstract AbstractObjectPath cutHead(int indexFrom);

    
    /**
     * Returns new instance of this path but without its tail 
     * @param indexFrom Cut index
     * @return new instance of this path
     */
    public abstract AbstractObjectPath cutTail(int indexFrom);
    
    /**
     * Returns string representation of this path sorted from root to leaf
     * @return string representation of this path
     */
    public String[] getPathFromRootToLeaf() {
        String[] array = Arrays.asList(this.pathFromRootToLeaf).toArray(new String[this.pathFromRootToLeaf.length]);
        return array;
    }

    /**
     * Returns string representation of this path sorted from leaf to root
     * @return string representation of this path
     */
    public String[] getPathFromLeafToRoot() {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(this.pathFromRootToLeaf));
        Collections.reverse(list);
        return list.toArray(new String[this.pathFromRootToLeaf.length]);
    }

    /**
     * Creates new path and injet into REPOSITORY object
     * @return nwe path with REPOSITORY object
     * @see SpecialObjects#REPOSITORY
     */
    public abstract AbstractObjectPath injectRepository();

    
    public abstract AbstractObjectPath injectObjectBetween(String injectingObject, Between between);

    
    public abstract AbstractObjectPath replace(String src, String dest);
    
    
	protected String[] injectInternal(String injectingObject, int bindex,
			int aindex) {
		String[] newpath = new String[this.pathFromRootToLeaf.length +1];
		System.arraycopy(this.pathFromRootToLeaf, 0, newpath, 0, bindex+1);
		newpath[aindex] = injectingObject;
		System.arraycopy(this.pathFromRootToLeaf, aindex, newpath, aindex+1, this.pathFromRootToLeaf.length-aindex);
		return newpath;
	}
    
    public static class Between {
    	private String before;
    	private String after;

    	public Between(String b, String a) {
    		this.before = b;
    		this.after =a;
    	}
    	
    	public String getBefore() {
			return before;
		}
    	
    	public String getAfter() {
			return after;
		}
    }
    
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
