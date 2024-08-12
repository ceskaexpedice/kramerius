/*
 * Copyright (C) Aug 1, 2024 Pavel Stastny
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
package cz.incad.kramerius.solr;

public class SolrKeys {
    
    /** Composite id */
    public static final String SOLR_SEARCH_USE_COMPOSITE_ID = "solrSearch.useCompositeId";

    /** Max value for hl.fragsize parameter */
    public static final int MAX_HL_FRAGSIZE = 120;
    /** Max value for hl.snippets parameter */
    public static final int MAX_HL_SNIPPETS = 10;
    /** Max value for combination of the parameters hl.fragsize*hl.snippets */
    public static final int MAX_HL_COMBINATION = 300;
    

    
    private SolrKeys( ) {}
}
