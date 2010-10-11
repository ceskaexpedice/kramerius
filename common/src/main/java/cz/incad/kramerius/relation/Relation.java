/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.relation;

import cz.incad.kramerius.KrameriusModels;

/**
 * Abstraction of Kramerius object relation.
 *
 * @author Jan Pokorsky
 */
public final class Relation {

    private final String pid;
    private final KrameriusModels kind;
    private transient int hashCache = -1;

    public Relation(String pid, KrameriusModels kind) {
        this.pid = pid;
        this.kind = kind;
    }

    public String getPID() {
        return pid;
    }

    public KrameriusModels getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Relation other = (Relation) obj;
        if ((this.pid == null) ? (other.pid != null) : !this.pid.equals(other.pid)) {
            return false;
        }
        if (this.kind != other.kind) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hashCache == -1) {
            int hash = 3;
            hash = 89 * hash + (this.pid != null ? this.pid.hashCode() : 0);
            hash = 89 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            hashCache = hash;
        }
        return hashCache;
    }

    @Override
    public String toString() {
        return String.format("Relation[%s, %s]", kind, pid);
    }
    
}
