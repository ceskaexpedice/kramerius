package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import java.io.IOException;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;

public class CollectionsRightsAdministration extends AbstractMainMenuItem implements AdminMenuItem {

    @Override
    public boolean isRenderable() {
        return  (hasUserAllowedAction(SecuredActions.VIRTUALCOLLECTION_MANAGE.getFormalName()));
    }

    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:globalActions.collectionActions(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.collectionsAction.title", false);
    }

    
}
