package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;

import java.io.IOException;

public class DNNTFlagUnset extends AbstractMainMenuItem implements AdminMenuItem {
    @Override
    public boolean isRenderable() {
        return (hasUserAllowedAction(SecuredActions.ADMINISTRATE.getFormalName()));
    }

    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:dnntFlagUnset(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.dnnt.unset.title", false);
    }

}
