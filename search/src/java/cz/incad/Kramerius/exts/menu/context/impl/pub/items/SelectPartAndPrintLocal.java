package cz.incad.Kramerius.exts.menu.context.impl.pub.items;

import java.io.IOException;

import cz.incad.Kramerius.exts.menu.context.impl.AbstractContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.pub.PublicContextMenuItem;
import cz.incad.Kramerius.tags.KConfigTag;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class SelectPartAndPrintLocal extends AbstractContextMenuItem implements PublicContextMenuItem {

    @Override
    public boolean isMultipleSelectSupported() {
        return false;
    }

    @Override
    public String getRenderedItem() throws IOException {
        String output = KConfiguration.getInstance().getConfiguration().getString("localprint.output", "html");
        return super.renderContextMenuItem("javascript:localprint.printPart('"+output+"');", "administrator.menu.selectandprintlocal",SecuredActions.SHOW_CLIENT_PRINT_MENU.getFormalName());
    }
}
