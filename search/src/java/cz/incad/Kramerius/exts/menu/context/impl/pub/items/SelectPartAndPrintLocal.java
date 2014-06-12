package cz.incad.Kramerius.exts.menu.context.impl.pub.items;

import java.io.IOException;

import cz.incad.Kramerius.exts.menu.context.impl.AbstractContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.pub.PublicContextMenuItem;

public class SelectPartAndPrintLocal extends AbstractContextMenuItem implements PublicContextMenuItem {

    @Override
    public boolean isMultipleSelectSupported() {
        return false;
    }

    @Override
    public String getRenderedItem() throws IOException {
        return super.renderContextMenuItem("javascript:printPartLocal();", "administrator.menu.selectandprintlocal");
    }
}
