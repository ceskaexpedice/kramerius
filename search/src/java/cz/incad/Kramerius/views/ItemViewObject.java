package cz.incad.Kramerius.views;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;

import cz.incad.Kramerius.exts.menu.context.ContextMenu;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;

public class ItemViewObject {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ItemViewObject.class.getName());

    @Inject
    ServletContext servletContext;

    @Inject
    MostDesirable mostDesirable;

    @Inject
    HttpServletRequest request;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    public ItemViewObject() {
        super();
    }

    /**
     * Only save to most desirable access
     * 
     * @return
     */
    public String getMostDesirableAccess() {
        mostDesirable.saveAccess(request.getParameter("pid"), new java.util.Date());
        return "";
    }

}
