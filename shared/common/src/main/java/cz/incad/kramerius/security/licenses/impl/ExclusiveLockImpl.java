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
package cz.incad.kramerius.security.licenses.impl;

import cz.incad.kramerius.security.licenses.ExclusiveLock;

public class ExclusiveLockImpl implements ExclusiveLock {
    
    private int refresh;
    private int max;
    private int readers;

    
    public ExclusiveLockImpl(int refresh, int max, int readers) {
        super();
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
    public String toString() {
        return "ExclusiveLockImpl [refresh=" + refresh + ", max=" + max + ", readers=" + readers + "]";
    }

    
}
