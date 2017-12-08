package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;

import java.io.IOException;

public class DataMigration extends AbstractMainMenuItem implements AdminMenuItem {
    @Override
    public boolean isRenderable() {
        return (hasUserAllowedPlanProcess("ndkmets"));
    }


    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:parametrizedProcess.open('data_migration'); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.datamigration.title", false);
    }

}
