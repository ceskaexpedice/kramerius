package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;

import java.io.IOException;

public class RebuildProcessingIndex extends AbstractMainMenuItem implements AdminMenuItem {

    @Override
    public boolean isRenderable() {
        return false;
    }

    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:rebuildProcessingIndex(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.rebuild_processing", false);
    }
}


//rebuildProcessingIndex