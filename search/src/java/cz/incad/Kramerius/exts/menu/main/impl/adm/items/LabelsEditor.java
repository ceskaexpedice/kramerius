package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;

import java.io.IOException;

public class LabelsEditor extends AbstractMainMenuItem implements AdminMenuItem {

    @Override
    public boolean isRenderable() {
        return  (hasUserAllowedAction(SecuredActions.CRITERIA_RIGHTS_MANAGE.getFormalName()));
    }

    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:labelsManager.showLabels(); javascript:hideAdminMenu();",
                "labels.menu.item.title", false);
    }

}
