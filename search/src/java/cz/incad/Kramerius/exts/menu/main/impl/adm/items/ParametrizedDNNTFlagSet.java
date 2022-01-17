package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;

import java.io.IOException;

public class ParametrizedDNNTFlagSet  extends AbstractMainMenuItem implements AdminMenuItem {

    public ParametrizedDNNTFlagSet() {

    }

    @Override
    public boolean isRenderable() {

        return (hasUserAllowedAction(SecuredActions.ADMINISTRATE.getFormalName()) ||
                hasUserAllowedAction(SecuredActions.DNNT_ADMIN.getFormalName()) ||
                hasUserAllowedPlanProcess("dnntset")
        );


    }
    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:parametrizedProcess.open('parametrizeddnntset'); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.dnnt.title", false);
    }
}