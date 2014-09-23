/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alberto
 */
public class RIndexHelper {

    private static final Logger logger = Logger.getLogger(RIndexHelper.class.getName());
    static IResourceIndex rindex;
    static String ERR_SELF_REF = "Self reference on {0}";

    public static ArrayList<String> getParentsArray(String pid) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {

        if (rindex == null) {
            rindex = ResourceIndexService.getResourceIndexImpl();
        }
        ArrayList<String> ret = rindex.getParentsPids(pid);

        if (ret.contains(pid)) {
            if (KConfiguration.getInstance().getConfiguration().getBoolean("k5indexer.continueOnError", false)) {
                // continuing
                logger.log(Level.WARNING, RIndexHelper.ERR_SELF_REF, pid);
                ret.remove(pid);
                Indexer.warnings++;
            } else {
                logger.log(Level.SEVERE, RIndexHelper.ERR_SELF_REF, pid);
                Indexer.errors++;
                throw new Exception(String.format(RIndexHelper.ERR_SELF_REF, pid));
            }

        }
        return ret;

    }

}
