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
package cz.incad.Kramerius.exts.menu.main.impl.pub;

import java.util.Set;

import com.google.inject.Inject;

import cz.incad.Kramerius.exts.menu.impl.AbstractMenuPart;
import cz.incad.Kramerius.exts.menu.main.MainMenuPart;

/**
 * Public menu part -> visible for everyone
 * @author pavels
 */
public class PublicMenuPartImpl extends AbstractMenuPart implements MainMenuPart {

    public static String FORMAL_NAME="PUBLIC";

    @Inject
    public PublicMenuPartImpl(Set<PublicMainMenuItem> items) {
        super();
        for (PublicMainMenuItem i : items) {
            this.items.add(i);
        }
    }

    
    @Override
    public String getFormalName() {
        return FORMAL_NAME;
    }

    @Override
    public boolean isRenderable() {
        return true;
    }

}
