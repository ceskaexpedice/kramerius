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

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import cz.incad.Kramerius.exts.menu.context.impl.AbstractContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.adm.AdminContextMenuItem;
import cz.incad.kramerius.processes.annotations.ParameterName;

public class ParametrizedPdfExport extends AbstractContextMenuItem implements AdminContextMenuItem  {


    
    @Override
    public boolean isMultipleSelectSupported() {
        return false;
    }

    @Override
    public boolean isRenderable() {
        boolean flag =  super.isRenderable();
        if (flag) return this.hasUserAllowedPlanProcess("export");
        return flag;
    }


    @Override
    public String getRenderedItem() throws IOException {
        Locale locale = localesProvider.get();
		String iso3country = locale.getISO3Country();
        String iso3lang =  locale.getISO3Language();
        
        return renderContextMenuItem(
                "javascript:parametrizedProcess.open('parametrized_static_export',{'country':'"+iso3country+"','lang':'"+iso3lang+"'}); javascript:hideAdminMenu();",
                "administrator.menu.export");

    }

    
}
