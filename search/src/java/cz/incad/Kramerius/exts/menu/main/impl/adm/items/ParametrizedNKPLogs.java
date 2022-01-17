package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;

import java.io.IOException;

public class ParametrizedNKPLogs extends AbstractMainMenuItem implements AdminMenuItem {

    public ParametrizedNKPLogs() { }

    @Override
    public boolean isRenderable() {

        return (hasUserAllowedAction(SecuredActions.ADMINISTRATE.getFormalName()) ||
                hasUserAllowedAction(SecuredActions.SHOW_STATISTICS.getFormalName())
        );


    }
    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:parametrizedProcess.open('nkplogs'); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.nkplogs.title", false);
    }

}
