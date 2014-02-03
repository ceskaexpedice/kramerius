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


import java.util.Arrays;

import cz.incad.kramerius.security.SpecialObjects;

/**
 * Represents objects path
 * @author pavels
 */
public class ObjectPidsPath extends AbstractObjectPath {
    
    /** REPOSITORY path constant */
    public static ObjectPidsPath REPOSITORY_PATH = new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid());
    
    public ObjectPidsPath(String ... pathFromRootToLeaf) {
        super(pathFromRootToLeaf);
    }
    
    @Override
    public ObjectPidsPath cutHead(int indexFrom) {
        return new ObjectPidsPath(super.cutHeadInternal(indexFrom));
    }



    @Override
    public ObjectPidsPath cutTail(int indexFrom) {
        return new ObjectPidsPath(super.cutTailInternal(indexFrom));
    }

    @Override
    public ObjectPidsPath injectRepository() {
        if (isEmptyPath()) return REPOSITORY_PATH;
        if (!this.pathFromRootToLeaf[0].equals(SpecialObjects.REPOSITORY.getPid())) {
            String[] args = new String[this.pathFromRootToLeaf.length+1];
            args[0] = SpecialObjects.REPOSITORY.getPid();
            System.arraycopy(this.pathFromRootToLeaf, 0, args, 1, this.pathFromRootToLeaf.length);
            return new ObjectPidsPath(args);
        } else return this;

    }

    
	@Override
	public ObjectPidsPath replace(String src, String dest) {
		String[] newpath = new String[this.pathFromRootToLeaf.length];
		for (int i = 0; i < newpath.length; i++) {
			String atom = this.pathFromRootToLeaf[i];
			if (atom.equals(src)) { atom = dest;}
			newpath[i]=atom;
		}
		return new ObjectPidsPath(newpath);
	}

	@Override
	public ObjectPidsPath injectObjectBetween(String injectingObject, Between between) {
    	if (between.getAfter() != null && between.getBefore() != null) {
    		int bindex = Arrays.asList(this.pathFromRootToLeaf).indexOf(between.getBefore());
    		int aindex = Arrays.asList(this.pathFromRootToLeaf).indexOf(between.getAfter());
    		if (Math.abs(bindex - aindex) == 1) {
				return new ObjectPidsPath(injectInternal(injectingObject, Math.min(bindex, aindex),Math.max(bindex, aindex)));
    		} else throw new IllegalArgumentException("ambiguous  injecting");
    	} else {
    		// the end || the beginning 
    		String object = between.getAfter() != null ? between.getAfter() : between.getBefore();
    		if (this.pathFromRootToLeaf[0].equals(object)) {
    			String[] newpath = new String[this.pathFromRootToLeaf.length +1];
    			newpath[0]=injectingObject;
    			System.arraycopy(this.pathFromRootToLeaf, 0, newpath, 1, this.pathFromRootToLeaf.length);
				return new ObjectPidsPath(newpath);
    		} else if (this.pathFromRootToLeaf[this.pathFromRootToLeaf.length - 1].equals(object)) {
    			String[] newpath = new String[this.pathFromRootToLeaf.length +1];
    			System.arraycopy(this.pathFromRootToLeaf, 0, newpath, 0, this.pathFromRootToLeaf.length);
    			newpath[newpath.length-1]= injectingObject;
				return new ObjectPidsPath(newpath);
    		} else throw new IllegalArgumentException("ambiguous  injecting");
    	}
	}
    
    
}
