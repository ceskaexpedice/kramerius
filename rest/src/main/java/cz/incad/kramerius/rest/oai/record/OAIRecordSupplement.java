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
package cz.incad.kramerius.rest.oai.record;


/**
 * Represents a supplemental record associated with an OAI record.
 * This supplement provides additional metadata or content information.
 *
 * @param data The unique identifier of the record in the Solr index.
 * @param supplementType The type of the supplement (e.g., first page of data).
 */
public record OAIRecordSupplement(
        Object data,
        SupplementType supplementType) {
}
