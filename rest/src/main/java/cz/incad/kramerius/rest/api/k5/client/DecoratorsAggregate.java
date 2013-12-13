/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.api.k5.client.item.display.DisplayType;

public class DecoratorsAggregate {

	List<Decorator> decorators = new ArrayList<Decorator>();

    @Inject
    public DecoratorsAggregate(Set<Decorator> decs) {
        super();
        for (Decorator p : decs) {
            this.decorators.add(p);
        }
    }

    public List<Decorator> getDecorators() {
    	return new ArrayList<Decorator>(this.decorators);
    }
}
