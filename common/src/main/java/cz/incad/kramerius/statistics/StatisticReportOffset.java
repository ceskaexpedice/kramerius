/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.kramerius.statistics;

import javax.ws.rs.GET;

/**
 * @author pavels
 *
 */
public class StatisticReportOffset {
    
    private int offset;
    private int size;
    
    private Object filteringValue;
    
    public StatisticReportOffset(int offset, int size, Object filteringValue) {
        super();
        this.offset = offset;
        this.size = size;
        this.filteringValue = filteringValue;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    public Object getFilteringValue() {
        return filteringValue;
    }

}
