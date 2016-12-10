package cz.incad.Kramerius.views.rights;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.json.JSONArray;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.security.strenderers.CollectionsWrapper;
import cz.incad.Kramerius.security.strenderers.SecuredActionWrapper;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.CollectionGet;
import cz.incad.kramerius.virtualcollections.VirtualCollectionsManager;

public class CollectionActionsView {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CollectionActionsView.class.getName());
    
    @Inject
    ResourceBundleService bundleService;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    CollectionGet colGet;

    public CollectionsWrapper[] getWrappers()  {
        try {
            JSONArray jsons = colGet.collections();
            CollectionsWrapper[] wraps = new CollectionsWrapper[jsons.length()];
            for (int i = 0; i < wraps.length; i++) {
                wraps[i] = new CollectionsWrapper(jsons.getJSONObject(i), localesProvider.get());
            }
            return wraps;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
