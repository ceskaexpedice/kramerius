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
 * Represents path of models
 * @author pavels
 */
public class ObjectModelsPath extends AbstractObjectPath {

    /** REPOSITORY Path constant */
    public static ObjectModelsPath REPOSITORY_PATH = new ObjectModelsPath(SpecialObjects.REPOSITORY.name());

    
    public ObjectModelsPath(String... pathFromRootToLeaf) {
        super(pathFromRootToLeaf);
    }

    
    
    @Override
    public ObjectModelsPath cutHead(int indexFrom) {
        return new ObjectModelsPath(super.cutHeadInternal(indexFrom));
    }



    @Override
    public ObjectModelsPath cutTail(int indexFrom) {
        return new ObjectModelsPath(super.cutTailInternal(indexFrom));
    }


    @Override
    public ObjectModelsPath injectRepository() {
        if (isEmptyPath()) return REPOSITORY_PATH;
        if (!this.pathFromRootToLeaf[0].equals(SpecialObjects.REPOSITORY.name())) {
            String[] args = new String[this.pathFromRootToLeaf.length];
            args[0] = SpecialObjects.REPOSITORY.getPid();
            System.arraycopy(this.pathFromRootToLeaf, 0, args, 1, this.pathFromRootToLeaf.length);
            return new ObjectModelsPath(args);
        } else return this;

    }
}
