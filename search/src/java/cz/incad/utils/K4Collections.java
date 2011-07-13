/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class K4Collections {
    
    public static <T> List<T> map(List<T> col, Mapper<T> mapper) {
        List<T> t = new ArrayList<T>();
        for (int i = 0,ll=col.size(); i < ll; i++) {
            t.add(mapper.process(col.get(i), i));
        }
        return t;
    }

    public static <T> T reduce(Combinator<T> combine, T base, List<T> list) {
        for (T item : list) {
            base = combine.process(base,item);
        }
        return base;
    }
        
    
    public static interface Combinator<T> {

        public T process(T base, T object);
    }
    

    public static interface Mapper<T> {
        
        public T process(T t, int index);
    }
}
