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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

public class JSONDecoratorsAggregate {

    Map<String, JSONDecorator> decoratorsMap = new HashMap<String, JSONDecorator>();
    List<String> keys = new ArrayList<String>();

    @Inject
    public JSONDecoratorsAggregate(Set<JSONDecorator> decs) {
        super();
        for (JSONDecorator p : decs) {
            String k = p.getKey();
            if (keys.contains(k)) {
                keys.remove(k);
            }
            keys.add(k);
            this.decoratorsMap.put(k, p);
        }
    }

    public List<JSONDecorator> getDecorators() {
        List<JSONDecorator> decorators = new ArrayList<JSONDecorator>();
        for (String k : this.keys) {
            decorators.add(this.decoratorsMap.get(k));
        }
        return decorators;
    }
}
