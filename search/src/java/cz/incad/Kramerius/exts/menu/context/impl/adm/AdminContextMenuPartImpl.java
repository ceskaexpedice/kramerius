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
package cz.incad.Kramerius.exts.menu.context.impl.adm;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.exts.menu.context.ContextMenuPart;
import cz.incad.Kramerius.exts.menu.impl.AbstractMenuPart;
import cz.incad.Kramerius.exts.menu.utils.GlobalRightsUtils;

public class AdminContextMenuPartImpl extends AbstractMenuPart implements ContextMenuPart {

    public static final String FORMAL_NAME="ADMIN";
    public static String SECURED_ACTION ="display_admin_menu";
    
    
    protected Provider<HttpServletRequest> requestProvider;
    
    @Inject
    public AdminContextMenuPartImpl(Provider<HttpServletRequest> requestProvider, Set<AdminContextMenuItem> items) {
        super();
        this.requestProvider = requestProvider;
        for (AdminContextMenuItem i : items) {
            this.items.add(i);
        }
    }

    @Override
    public String getFormalName() {
        return FORMAL_NAME;
    }

    @Override
    public boolean isRenderable() {
        boolean right = GlobalRightsUtils.hasUserAllowedAction(SECURED_ACTION, this.requestProvider.get());
        return right;
    }


}
