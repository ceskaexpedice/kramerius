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

package cz.incad.kramerius.relation.impl;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Jan Pokorsky
 */
public final class RelationModelImpl implements RelationModel {

    private final String pid;
    private final Map<KrameriusModels, List<Relation>> rels;
    private final KrameriusModels kind;

    RelationModelImpl(String pid, Map<KrameriusModels, List<Relation>> rels, KrameriusModels kind) {
        this.pid = pid;
        this.rels = rels;
        this.kind = kind;
    }

    public RelationModelImpl(String pid, KrameriusModels kind) {
        this(pid, new EnumMap<KrameriusModels, List<Relation>>(KrameriusModels.class), kind);
    }

    @Override
    public List<Relation> getRelations(KrameriusModels relationsKind) {
        List<Relation> res = rels.get(relationsKind);
        return res;
    }

    @Override
    public boolean addRelationKind(KrameriusModels kind) {
        if (rels.containsKey(kind)) {
            return false;
        } else {
            rels.put(kind, new ArrayList<Relation>());
            return true;
        }
    }

    @Override
    public boolean removeRelationKind(KrameriusModels kind) {
        // do not remove kind key as it is neccessary to proper save the model
        boolean modified = false;
        List<Relation> kindRels = rels.get(kind);
        if (kindRels != null) {
            kindRels.clear();
            modified = true;
        }
        return modified;
    }

    @Override
    public Set<KrameriusModels> getRelationKinds() {
        return Collections.unmodifiableSet(rels.keySet());
    }

    @Override
    public KrameriusModels getKind() {
        return kind;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (Iterator<Entry<KrameriusModels, List<Relation>>> mapIter = rels.entrySet().iterator()
                ; mapIter.hasNext();) {

            Entry<KrameriusModels, List<Relation>> entry = mapIter.next();
            sb.append(entry.getKey()).append("=[");

            for (Iterator<Relation> listIter = entry.getValue().iterator(); listIter.hasNext();) {
                Relation relation = listIter.next();
                sb.append(relation);
                if (listIter.hasNext()) {
                    sb.append(",");
                }
            }

            sb.append("]");
            if (mapIter.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("}");
        return String.format("%s[%s, %s, [%s]]", super.toString(), pid, kind, sb);
    }

    String getPid() {
        return pid;
    }

    /** SPI to trim model after save */
    void onSave() {
        for (Iterator<Entry<KrameriusModels, List<Relation>>> it = rels.entrySet().iterator(); it.hasNext();) {
            Entry<KrameriusModels, List<Relation>> entry = it.next();
            if (entry.getValue().isEmpty()) {
                it.remove();
            }
        }
    }

}
