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
package cz.incad.kramerius.rest.api.processes.filter;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.utils.database.SQLFilter.ConverterAndFormatter;

public class StatusConverter implements ConverterAndFormatter{

	@Override
	public Object convert(String strVal) {
        States st = States.valueOf(strVal);
        return st.getVal();
	}

	@Override
	public String format(Object val) {
		States st = (States) val;
		return null;
	}
}
