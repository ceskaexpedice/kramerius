package cz.incad.kramerius.utils;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;

public class TetSolrAccess {
    
    public static void main(String[] args) throws IOException, TransformerException {
        SolrAccess sa = new SolrAccessImplNewIndex();
        Document solrDataByPid = sa.getSolrDataByPid("uuid:91771660-484f-11dd-b5f2-000d606f5dc6");
        System.out.println(solrDataByPid);
        XMLUtils.print(solrDataByPid, System.out);
        
        ObjectPidsPath[] ownPidPaths = sa.getOwnPidPaths(solrDataByPid);
        System.out.println(ownPidPaths.length);
        for (ObjectPidsPath omp : ownPidPaths) {
            System.out.println(omp.toString());
        }
        System.out.println("KONEC");
    }
}
