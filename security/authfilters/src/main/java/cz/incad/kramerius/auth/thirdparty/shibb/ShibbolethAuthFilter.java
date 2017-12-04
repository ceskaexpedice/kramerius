package cz.incad.kramerius.auth.thirdparty.shibb;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.auth.thirdparty.ExtAuthFilter;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUtils;

public abstract class ShibbolethAuthFilter extends ExtAuthFilter {



    public boolean userStoreIsNeeded(HttpServletRequest httpReq) {
        if (ShibbolethUtils.isUnderShibbolethSession(httpReq)
                && (!ShibbolethUtils.isShibbolethSessionIsStored(httpReq))) {
            return true;
        } else if (ShibbolethUtils.isUnderShibbolethSession(httpReq)
                        && ShibbolethUtils.isShibbolethSessionIsStored(httpReq)
                        && (!ShibbolethUtils.validateShibbolethSessionId(httpReq))) {
            return true;
        }
        return false;
    }

}
