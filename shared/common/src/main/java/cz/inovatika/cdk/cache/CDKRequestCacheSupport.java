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
package cz.inovatika.cdk.cache;

import java.sql.SQLException;
import java.util.List;


/**
 * Interface for caching CDK requests. This cache allows caching query results
 * for individual instances of Kramerius so that they do not need to be retrieved again.
 * It is primarily used for queries related to rights, descriptors, metadata, and thumbnails.
 */
public interface CDKRequestCacheSupport {

    /**
     * Retrieves a list of cached request items based on the provided parameters.
     *
     * @param dlAcronym          The acronym of the digital library.
     * @param url                The URL of the requested resource.
     * @param pid                The persistent identifier of the resource.
     * @param userIdentification The identification of the user making the request.
     * @return A list of {@link CDKRequestItem} objects matching the given parameters,
     *         or an empty list if no matching items are found.
     */
    List<CDKRequestItem> find(String dlAcronym, String url, String pid, String userIdentification);

    /**
     * Saves a {@link CDKRequestItem} to the cache.
     *
     * @param item The request item to be stored.
     * @throws SQLException If a database error occurs while saving the item.
     */
    void save(CDKRequestItem item) throws SQLException;

    /**
     * Removes a specific request item from the cache.
     *
     * @param item The request item to be removed.
     */
    void remove(CDKRequestItem item);

    /**
     * Removes multiple request items from the cache.
     *
     * @param items A list of request items to be removed.
     */
    void remove(List<CDKRequestItem> items);

    /**
     * Removes all request items associated with a given persistent identifier (PID).
     *
     * @param pid The persistent identifier whose associated items should be removed.
     */
    void removeByPid(String pid);
}
