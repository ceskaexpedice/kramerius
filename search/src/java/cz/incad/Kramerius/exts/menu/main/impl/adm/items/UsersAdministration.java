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
package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import java.io.IOException;

import com.google.inject.Inject;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUtils;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class UsersAdministration extends AbstractMainMenuItem implements AdminMenuItem {

    @Inject
    KConfiguration kConfiguration;
    
    @Override
    public boolean isRenderable() {
        return (hasUserAllowedAction(SecuredActions.ADMINISTRATE.getFormalName()));
    }

    @Override
    public String getRenderedItem() throws IOException {
      String href = kConfiguration.getUsersEditorURL();
      return renderMainMenuItem(href, "administrator.menu.userseditor",true);
    }

}
