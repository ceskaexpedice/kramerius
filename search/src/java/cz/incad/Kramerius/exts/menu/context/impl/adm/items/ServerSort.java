package cz.incad.Kramerius.exts.menu.context.impl.adm.items;

import java.io.IOException;

import cz.incad.Kramerius.exts.menu.context.impl.AbstractContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.adm.AdminContextMenuItem;
import cz.incad.kramerius.security.SecuredActions;

public class ServerSort extends AbstractContextMenuItem implements AdminContextMenuItem {

	@Override
	public String getRenderedItem() throws IOException {
        return super.renderContextMenuItem("javascript:serverSort();", "administrator.menu.sort");
	}

	
	
	@Override
	public boolean isRenderable() {
        boolean flag =  super.isRenderable();
        if (flag) return this.hasUserAllowedAction(SecuredActions.SORT.getFormalName());
        return flag;
	}



	@Override
	public boolean isMultipleSelectSupported() {
		return false;
	}

}
