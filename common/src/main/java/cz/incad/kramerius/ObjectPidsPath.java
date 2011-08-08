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


import cz.incad.kramerius.security.SpecialObjects;

/**
 * Represents path to object. From root to leaf.
 * @author pavels
 */
public class ObjectPidsPath extends AbstractObjectPath {

    public static ObjectPidsPath REPOSITORY_PATH = new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid());
    
    public ObjectPidsPath(String ... pathFromRootToLeaf) {
        super(pathFromRootToLeaf);
    }
    
    public ObjectPidsPath injectRepository() {
        if (isEmptyPath()) return REPOSITORY_PATH;
        if (!this.pathFromRootToLeaf[0].equals(SpecialObjects.REPOSITORY.getPid())) {
            String[] args = new String[this.pathFromRootToLeaf.length+1];
            args[0] = SpecialObjects.REPOSITORY.getPid();
            System.arraycopy(this.pathFromRootToLeaf, 0, args, 1, this.pathFromRootToLeaf.length);
            return new ObjectPidsPath(args);
        } else return this;

    }
}
