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

import cz.incad.kramerius.rest.oai.metadata.decorators.impl.AddCreatedFromDateDecorator;
import cz.incad.kramerius.rest.oai.metadata.decorators.impl.AuthorFromModsDecorator;
import cz.incad.kramerius.rest.oai.metadata.decorators.impl.NoLangueDecorator;
import cz.incad.kramerius.rest.oai.metadata.decorators.impl.NoTypeOrSubjectDecorator;
import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Chain of Dublin Core decorators that are sequentially applied
 * to a Dublin Core XML document.
 * <p>
 * This class implements the decorator chain pattern, where multiple
 * {@link MetadataDecorator} instances can be composed and executed
 * in a defined order to enrich the metadata.
 * </p>
 */
public class DecoratorsChain {

    public static final Logger LOGGER = Logger.getLogger(DecoratorsChain.class.getName());

    private List<MetadataDecorator> decorators = Arrays.asList(
            new NoLangueDecorator(),
            new NoTypeOrSubjectDecorator(),
            new AddCreatedFromDateDecorator(),
            new AuthorFromModsDecorator()
    );


    /**
     * Applies all configured decorators to the provided Dublin Core document
     * in the order they are defined in the chain.
     *
     * @param dc The original Dublin Core XML document.
     * @return The decorated Dublin Core document after applying all decorators.
     */
    public Document dublinCoreDecorate(Document dc, Document mods) {
        LOGGER.info(" Starting Dublin Core decoration with " + this.decorators.size() + " decorators.");
        LOGGER.info(" MODS instance  " + mods);

        Document processDoc = dc;
        for (MetadataDecorator decorator : this.decorators) {
            processDoc = decorator.decorate(processDoc, mods);
        }
        return processDoc;
    }
}
