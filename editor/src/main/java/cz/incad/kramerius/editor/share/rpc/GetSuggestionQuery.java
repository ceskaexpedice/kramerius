/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.editor.share.rpc;

import net.customware.gwt.dispatch.shared.Action;

/**
 *
 * @author Jan Pokorsky
 */
public final class GetSuggestionQuery implements Action<GetSuggestionResult> {

    private String filter;
    private int limit;

    /* gwt serialization purposes */
    private GetSuggestionQuery() {
    }

    public GetSuggestionQuery(String filter, int limit) {
        this.filter = filter;
        this.limit = limit;
    }

    public String getFilter() {
        return filter;
    }

    public int getLimit() {
        return limit;
    }

}
