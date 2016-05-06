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
package cz.incad.Kramerius.exts.menu.main.impl.pub.items;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.pub.PublicMainMenuItem;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUtils;

public class ChangePassword extends AbstractMainMenuItem implements PublicMainMenuItem {

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Override
    public boolean isRenderable() {
        return (!ShibbolethUtils.isUnderShibbolethSession(this.requestProvider.get()));
    }

    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
            "javascript:(new ChangePswd()).changePassword(); javascript:hideAdminMenu();",
        "administrator.menu.dialogs.changePswd.title", false);
    }
}
