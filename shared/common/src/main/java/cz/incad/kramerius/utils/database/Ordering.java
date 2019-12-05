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
package cz.incad.kramerius.utils.database;

import java.util.ArrayList;
import java.util.List;

public class Ordering {
	
	private List<String> allcols = new ArrayList<String>();
	private String selected = null;
	
	public Ordering(String ... all) {
		super();
		for (String or : all) {
			this.allcols.add(or);
		}
	}
	
	public Ordering select(String col) {
		for (String c : this.allcols) {
			if (c.equals(col)) {
				this.selected = col;
				return this;
			}
		}
		throw new IllegalArgumentException("excepting column name !");
	}

	public String getSelected() {
		return this.selected;
	}

	public String[] getColumn() {
		return (String[]) this.allcols.toArray(new String[this.allcols.size()]);
	}
	
}
