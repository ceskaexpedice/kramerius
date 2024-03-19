/*
 * Copyright (C) Feb 26, 2024 Pavel Stastny
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
package cz.incad.kramerius.security.licenses.impl.lock;

import org.apache.commons.codec.digest.DigestUtils;

import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock;

public class ExclusiveLockImpl implements ExclusiveLock {
    
    private ExclusiveLockType type = ExclusiveLockType.INSTANCE;
    
    private int refresh;
    private int max;
    private int readers;

    
    public ExclusiveLockImpl(int refresh, int max, int readers) {
        super();
        this.refresh = refresh;
        this.max = max;
        this.readers = readers;
    }
    
    

    
    
    public ExclusiveLockImpl(int refresh, int max, int readers, ExclusiveLockType type) {
        super();
        this.type = type;
        this.refresh = refresh;
        this.max = max;
        this.readers = readers;
    }


    @Override
    public int getRefreshInterval() {
        return this.refresh;
    }

    @Override
    public int getMaxInterval() {
        return this.max;
    }

    @Override
    public int getMaxReaders() {
        return this.readers;
    }

    @Override
    public ExclusiveLockType getType() {
        return this.type;
    }
    
    public void setType(ExclusiveLockType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "ExclusiveLockImpl [refresh=" + refresh + ", max=" + max + ", readers=" + readers + "]";
    }
    
    
    

    @Override
    public String createLockHash(License license, Right right, String pid) {
        switch(this.type) {
            case INSTANCE: 
                String instanceHashName = String.format("%s_%s", license.getName(), pid);
                return instanceHashName;
                //return DigestUtils.md5Hex(instanceHashName);
            case RULE: 
                String ruleHashName = String.format("%s_%s_%d_%s", license.getName(), pid, right.getId(), right.getRole().getName());
                return ruleHashName;
                //return DigestUtils.md2Hex(ruleHashName);
        }
        return null;
    }

    
    
}
