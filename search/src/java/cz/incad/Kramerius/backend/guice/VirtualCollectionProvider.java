package cz.incad.Kramerius.backend.guice;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionsManager;

public class VirtualCollectionProvider implements Provider<Collection> {

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration kConfiguration;

    @Inject
    @Named("fedora")
    CollectionsManager manager;
    
    public static final String VIRTUAL_COLLECTION = "virtual_collection";
    private Provider<HttpServletRequest> provider;
    private Logger logger;

    @Inject
    public VirtualCollectionProvider(Provider<HttpServletRequest> provider, Logger logger) {
        super();
        this.provider = provider;
        this.logger = logger;
    }

    @Override
    public Collection get() {
        try {
            HttpServletRequest request = this.provider.get();
            HttpSession session = request.getSession(true);
            String parameter = request.getParameter("collection");
            if (parameter != null) {
                if (parameter.equals("none")) {
                    session.setAttribute(VIRTUAL_COLLECTION, null);
                    return null;
                } else {
                    if (!parameter.startsWith("vc:")) {
                        parameter = "vc:" + parameter;
                    }
                    //ArrayList<String> langs = new ArrayList<String>(Arrays.asList(kConfiguration.getPropertyList("interface.languages")));
                    Collection vc = this.manager.getCollection(parameter);
                    session.setAttribute(VIRTUAL_COLLECTION, vc);
                    return vc;
                }

            } else if (session.getAttribute(VIRTUAL_COLLECTION) != null) {
                return (Collection) session.getAttribute(VIRTUAL_COLLECTION);
            } else {
                return null;
            }
        } catch (CollectionException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }
    

    private boolean isInUrl(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        return requestURL.contains("/vc/");
    }

    private String disectURL(String requestURL) {
        return requestURL.substring(requestURL.indexOf("/vc/") + "/vc/".length());

    }
}
