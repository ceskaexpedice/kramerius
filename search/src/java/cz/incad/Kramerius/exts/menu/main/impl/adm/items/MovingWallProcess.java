package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import java.io.IOException;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;

public class MovingWallProcess extends AbstractMainMenuItem implements AdminMenuItem {

    @Override
    public boolean isRenderable() {
        return (hasUserAllowedAction(SecuredActions.ADMINISTRATE.getFormalName()));
    }

    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:parametrizedProcess.open('parametrized_iterated_appliedmw'); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.mw.title", false);
    }

}
