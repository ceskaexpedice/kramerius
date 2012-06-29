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
package cz.incad.Kramerius.exts.menu.impl;

import java.util.ArrayList;
import java.util.List;

import cz.incad.Kramerius.exts.menu.MenuItem;
import cz.incad.Kramerius.exts.menu.MenuPart;

public abstract class AbstractMenuPart implements MenuPart {
    
    protected List<MenuItem> items = new ArrayList<MenuItem>();
    
    @Override
    public MenuItem[] getItems() {
        return (MenuItem[]) this.items.toArray(new MenuItem[this.items.size()]);
    }

}
