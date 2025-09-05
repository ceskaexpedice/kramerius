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
package cz.incad.kramerius.rest.oai.metadata.decorators;

import org.w3c.dom.Document;

/**
 * Interface for Dublin Core decorators used to enrich or modify
 * the Dublin Core metadata of OAI records.
 * <p>
 * Implementations of this interface apply specific enrichment
 * rules to a provided Dublin Core XML {@link Document}.
 * </p>
 */
public interface MetadataDecorator {

    /**
     * Enriches or modifies the provided Dublin Core document according
     * to the decorator's logic.
     *
     * @param dc   The original Dublin Core XML document
     * @param mods Biblo mods document
     * @return The decorated (possibly modified) Dublin Core document.
     */
    public Document decorate(Document dc, Document mods);

}
