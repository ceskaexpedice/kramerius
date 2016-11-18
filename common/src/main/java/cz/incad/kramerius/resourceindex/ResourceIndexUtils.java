package cz.incad.kramerius.resourceindex;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ResourceIndexUtils {

    public static final Logger LOGGER = Logger.getLogger(ResourceIndexUtils.class.getName());
    
    /**
     * Moved from MPTStore; used in all implementetion of the resource index interface
     * @param pid
     * @param resIndex
     * @return
     * @throws ResourceIndexException
     */
    public static List<String> getPidPaths(String pid, IResourceIndex resIndex) throws ResourceIndexException {
        LOGGER.info(pid);
        List<String> resList = new ArrayList<String>();
        List<String> parents = resIndex.getParentsPids(pid);
        LOGGER.info(parents.toString());
        for (int i = 0; i < parents.size(); i++) {
            List<String> grands = resIndex.getPidPaths(parents.get(i));
            LOGGER.info(grands.toString());
            if (grands.isEmpty()) {
                resList.add(parents.get(i));
            } else {
                for (int j = 0; j < grands.size(); j++) {
                    resList.add(grands.get(j) + "/" + parents.get(i));
                }
            }
        }
        return resList;
    }

}   
