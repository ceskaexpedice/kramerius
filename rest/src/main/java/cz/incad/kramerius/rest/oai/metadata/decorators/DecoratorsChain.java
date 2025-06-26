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

import cz.incad.kramerius.rest.oai.metadata.decorators.impl.NoLangueDecorator;
import cz.incad.kramerius.rest.oai.metadata.decorators.impl.NoTypeOrSubjectDecorator;
import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.List;

public class DecoratorsChain {

    private List<DublinCoreDecorator> decorators = Arrays.asList(
            new NoLangueDecorator(),
            new NoTypeOrSubjectDecorator()
    );


    public Document decorate(Document doc) {
        Document processDoc = doc;
        for (DublinCoreDecorator decorator : this.decorators) {
            processDoc = decorator.decorate(processDoc);
        }
        return processDoc;
    }
}
