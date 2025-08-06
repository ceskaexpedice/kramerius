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
import cz.incad.kramerius.rest.oai.metadata.decorators.impl.NoLangueDecorator;
import cz.incad.kramerius.rest.oai.metadata.decorators.impl.NoTypeOrSubjectDecorator;
import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.List;

/**
 * Chain of Dublin Core decorators that are sequentially applied
 * to a Dublin Core XML document.
 * <p>
 * This class implements the decorator chain pattern, where multiple
 * {@link DublinCoreDecorator} instances can be composed and executed
 * in a defined order to enrich the metadata.
 * </p>
 */
public class DecoratorsChain {

    private List<DublinCoreDecorator> decorators = Arrays.asList(
            new NoLangueDecorator(),
            new NoTypeOrSubjectDecorator(),
            new AddCreatedFromDateDecorator()
    );


    /**
     * Applies all configured decorators to the provided Dublin Core document
     * in the order they are defined in the chain.
     *
     * @param doc The original Dublin Core XML document.
     * @return The decorated Dublin Core document after applying all decorators.
     */
    public Document decorate(Document doc) {
        Document processDoc = doc;
        for (DublinCoreDecorator decorator : this.decorators) {
            processDoc = decorator.decorate(processDoc);
        }
        return processDoc;
    }
}
