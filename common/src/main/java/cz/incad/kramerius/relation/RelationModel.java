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
import java.util.List;
import java.util.Set;

/**
 * Model of RELS-EXT stream of a Kramerius object. It contains ordered relations
 * to other Kramerius objects.
 * <p/>The model allows change relations in memory. For
 * IO operations use {@link RelationService}.
 *
 * @author Jan Pokorsky
 */
public interface RelationModel {

    /**
     * Gets kind of the Kramerius object associated with the model.
     *
     * @return the kind
     */
    KrameriusModels getKind();

    /**
     * Gets unmodifiable set of relation kinds.
     * @return the set of kinds
     */
    Set<KrameriusModels> getRelationKinds();

    /**
     * Adds new relation kind to model.
     *
     * @param kind a relation kind to add
     * @return {@code true} if the model changed
     */
    boolean addRelationKind(KrameriusModels kind);

    /**
     * Removes relation kind and all related relations from the model.
     *
     * @param kind a relation kind to remove
     * @return {@code true} if the model changed
     */
    boolean removeRelationKind(KrameriusModels kind);

    /**
     * Gets live list of relations that the client may change.
     * @param relationKind the kind of relations to retrieve
     * @return the list of relations
     */
    List<Relation> getRelations(KrameriusModels relationKind);

}
