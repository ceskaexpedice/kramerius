package cz.incad.kramerius.indexer.xslt;

import cz.incad.kramerius.indexer.FedoraOperations;
import cz.incad.kramerius.indexer.guice.GuiceModelInjector;

public class XSLTSupport {
    
    public XSLTSupport() {
        
    }
    
    public String prepareCzech(String s) throws Exception {
        FedoraOperations fo = GuiceModelInjector.injector().getInstance(FedoraOperations.class);
        return fo.prepareCzech(s);
    }
    
    public String getDatastreamText(String pid, String dsId, String pageNum) throws Exception {
        FedoraOperations fo = GuiceModelInjector.injector().getInstance(FedoraOperations.class);
        return fo.getDatastreamText(pid, dsId, pageNum);
    }
}
