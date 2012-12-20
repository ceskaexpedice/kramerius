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
package cz.incad.Kramerius.exts.menu.context.impl.adm.items;

import java.io.IOException;

import com.google.inject.Inject;

import cz.incad.Kramerius.exts.menu.context.impl.AbstractContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.adm.AdminContextMenuItem;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Metadata editor
 * @author pavels
 */
public class Editor extends AbstractContextMenuItem implements AdminContextMenuItem {


    @Override
    public String getRenderedItem() throws IOException {
        return super.renderContextMenuItem("javascript:openEditor('"+this.configuration.getEditorURL()+"');", "administrator.menu.editor");
    }
    
    @Override
    public boolean isRenderable() {
        boolean flag =  super.isRenderable();
        if (flag) return this.hasUserAllowedAction(SecuredActions.EDITOR.getFormalName());
        return flag;
    }

    @Override
    public boolean isMultipleSelectSupported() {
        return false;
    }
    
}
