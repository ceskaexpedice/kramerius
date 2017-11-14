package cz.incad.kramerius.virtualcollections.impl.fedora;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.utils.FedoraUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionUtils;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.impl.AbstractCollectionManager;

/**
 * Implementation stands on fedora
 * @author pstastny
 *
 */
public class FedoraCollectionsManagerImpl extends AbstractCollectionManager {

    public static final Logger LOGGER = Logger.getLogger(FedoraCollectionsManagerImpl.class.getName());

    public FedoraCollectionsManagerImpl() {
        super();
    }
    
    @Override
    protected List<String> languages() {
        return super.languages();
    }
    
    private Collection findCollection(String pid,List<Collection> cols) {
        for (Collection cl : cols) {
            if (cl.getPid().equals(pid)) return cl;
        }
        return null;
    }
    
    @Override
    public List<Collection> getCollections() throws CollectionException {
        try {
            List<Collection> cols = new ArrayList<>();
            IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
            List<String> collectionPids = g.getCollections();
            for (String cPid : collectionPids) {
                if (findCollection(cPid, cols) == null) {
                    if (this.fa.isObjectAvailable(cPid)) {
                        if (this.fa.isStreamAvailable(cPid, FedoraUtils.DC_STREAM)) {
                            Document dc = fa.getDC(cPid);
                            Collection col = new Collection(cPid, dcTitle(dc), dcType(dc));
                            enhanceNumberOfDocs(col);
                            enhanceDescriptions(col);
                            cols.add(col);
                        } else {
                            LOGGER.warning("Collection '"+cPid+"' doesn't defined DC Stream - title is missing, canLeave flag is missing");
                            Collection col = new Collection(cPid, "no-name", true);
                            enhanceNumberOfDocs(col);
                            cols.add(col);

                        }
                    } else {
                        LOGGER.warning("Collection '"+cPid+"' doesn't exist");
                        //throw new CollectionException("Collection '"+cPid+"' doesn't exist");
                    }
                }
            }
            return cols;
        } catch (ClassNotFoundException e) {
            throw new CollectionException(e);
        } catch (InstantiationException e) {
            throw new CollectionException(e);
        } catch (IllegalAccessException e) {
            throw new CollectionException(e);
        } catch (DOMException e) {
            throw new CollectionException(e);
        } catch (IOException e) {
            throw new CollectionException(e);
        } catch (Exception e) {
            throw new CollectionException(e);
        }
    }




}
