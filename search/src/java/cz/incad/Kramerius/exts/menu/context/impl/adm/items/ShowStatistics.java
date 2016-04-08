package cz.incad.Kramerius.exts.menu.context.impl.adm.items;

import java.io.IOException;

import cz.incad.Kramerius.exts.menu.context.impl.AbstractContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.adm.AdminContextMenuItem;
import cz.incad.kramerius.security.SecuredActions;

public class ShowStatistics extends AbstractContextMenuItem implements AdminContextMenuItem {

	
	@Override
	public boolean isRenderable() {
        boolean flag =  super.isRenderable();
        if (flag) return this.hasUserAllowedAction(SecuredActions.SHOW_STATISTICS.getFormalName());
        return flag;
	}

	@Override
	public boolean isMultipleSelectSupported() {
		return true;
	}

	@Override
	public String getRenderedItem() throws IOException {
        return super.renderContextMenuItem("javascript:statistics.showContextDialog();", "administrator.menu.dialogs.statistics.title", SecuredActions.SHOW_STATISTICS.getFormalName());
	}
}
