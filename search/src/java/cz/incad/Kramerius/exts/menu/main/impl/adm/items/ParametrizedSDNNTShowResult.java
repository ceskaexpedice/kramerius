package cz.incad.Kramerius.exts.menu.main.impl.adm.items;

import java.io.IOException;

import cz.incad.Kramerius.exts.menu.main.impl.AbstractMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ParametrizedSDNNTShowResult   extends AbstractMainMenuItem implements AdminMenuItem {

    public ParametrizedSDNNTShowResult() {}

    @Override
    public boolean isRenderable() {

    	boolean acronymSet = StringUtils.isAnyString(KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.acronym"));
    	boolean apiSet = StringUtils.isAnyString(
    			KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.local.api", KConfiguration.getInstance().getConfiguration().getString("api.point")));
    	boolean endpointSet = StringUtils.isAnyString(
    			KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.endpoint",
                        "https://sdnnt.nkp.cz/sdnnt/api/v1.0/lists/changes")    			
    			);
    	boolean allSet = acronymSet && apiSet && endpointSet;
    	
    	return ( allSet &&
    			(hasUserAllowedAction(SecuredActions.ADMINISTRATE.getFormalName()) ||
                hasUserAllowedAction(SecuredActions.DNNT_ADMIN.getFormalName()) ||
                hasUserAllowedPlanProcess("dnntset"))
        );


    }
    @Override
    public String getRenderedItem() throws IOException {
        return renderMainMenuItem(
                "javascript:showSdnntSync(); javascript:hideAdminMenu();",
                "administrator.menu.dialogs.sdnntsync.title", false);
    }
}