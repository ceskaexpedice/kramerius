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
package cz.incad.Kramerius.exts.menu.main.impl;

import java.util.Set;

import com.google.inject.Inject;

import cz.incad.Kramerius.exts.menu.MenuPart;
import cz.incad.Kramerius.exts.menu.impl.AbstractMenu;
import cz.incad.Kramerius.exts.menu.main.MainMenu;
import cz.incad.Kramerius.exts.menu.main.MainMenuPart;

/**
 * Main menu implementation
 * @author pavels
 */
public class MainMenuImpl extends AbstractMenu implements MainMenu {

    @Inject
    public MainMenuImpl(Set<MainMenuPart> parts) {
        super();
        for (MenuPart p : parts) {
            this.parts.add(p);
        }
    }

}
