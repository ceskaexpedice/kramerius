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
import java.util.Locale;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class MetadataEditor extends AbstractMainMenuItem implements AdminMenuItem {

    @Inject
    KConfiguration kconfig;
    
    @Inject
    Provider<Locale> provider;
    
    @Override
    public boolean isRenderable() {
        return (hasUserAllowedAction(SecuredActions.EDITOR.getFormalName()));
    }

    @Override
    public String getRenderedItem() throws IOException {
      String localeParam = this.provider.get() == null ? "" : "?locale=" + this.provider.get().getLanguage();
      String href = kconfig.getEditorURL() + localeParam;
      return renderMainMenuItem(href, "administrator.menu.dialogs.editor.title", true);
    }


}
