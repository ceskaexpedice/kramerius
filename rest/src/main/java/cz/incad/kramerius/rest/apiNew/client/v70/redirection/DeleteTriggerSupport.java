/*
 * Copyright (C) 2025  Inovatika
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
package cz.incad.kramerius.rest.apiNew.client.v70.redirection;

/**
 * Interface for handling events related to possible inconsistencies in an index.
 * This interface provides methods to trigger actions upon detecting conflicts or deletions.
 */
public interface DeleteTriggerSupport {

    /**
     * Executes a conflict trigger for a given identifier.
     * This method should be called when a potential inconsistency is detected in the index.
     *
     * @param pid The unique identifier of the entity related to the detected conflict.
     */
    public void executeConflictTrigger(String pid);

    /**
     * Executes a delete trigger for a given identifier.
     * This method should be called when an entity needs to be removed from the index.
     *
     * @param pid The unique identifier of the entity to be deleted.
     */
    public void executeDeleteTrigger(String pid);
}
