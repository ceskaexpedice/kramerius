package cz.incad.Kramerius.exts.menu.context.impl.adm.items;

import java.io.IOException;

import cz.incad.Kramerius.exts.menu.context.impl.AbstractContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.adm.AdminContextMenuItem;

public class ApplyMovingWallItem extends AbstractContextMenuItem implements AdminContextMenuItem  {

    
    @Override
    public boolean isMultipleSelectSupported() {
        return false;
    }


    @Override
    public String getRenderedItem() throws IOException {
        return super.renderContextMenuItem("javascript:parametrizedProcess.open('parametrizedapplymw');", "administrator.menu.applymw");
    }

    
}
