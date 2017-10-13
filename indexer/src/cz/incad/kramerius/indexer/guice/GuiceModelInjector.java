package cz.incad.kramerius.indexer.guice;

import java.io.ByteArrayInputStream;

import org.w3c.dom.Document;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import cz.incad.kramerius.indexer.FedoraOperations;
import cz.incad.kramerius.indexer.SolrOperations;
import cz.incad.kramerius.indexer.fa.FedoraAccessBridge;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.XMLUtils;

public class GuiceModelInjector extends AbstractModule {

    private static Injector _injectorInstance;

    @Override
    protected void configure() {
        bind(FedoraAccessBridge.class);
        bind(SolrOperations.class);
        bind(FedoraOperations.class);
    }

    @Provides
    public StatisticsAccessLog get() {
        return null;
    }
    
    public synchronized static Injector injector() {
        if (_injectorInstance == null) {
            _injectorInstance = Guice.createInjector(
                    new RepoModule(), new GuiceModelInjector());
        }
        return _injectorInstance;
    }
    
    public static void main(String[] args) throws Exception {
        Injector injector = GuiceModelInjector.injector();
        //FedoraAccess instance = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        FedoraAccessBridge bridge = injector.getInstance(FedoraAccessBridge.class);
        byte[] foxml = bridge.getFoxml("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");
        System.out.println(new String(foxml));
        Document parseDocument = XMLUtils.parseDocument(new ByteArrayInputStream(foxml), true);
        FedoraOperations fedoraOp = new FedoraOperations(bridge);
        SolrOperations solrOp = new SolrOperations(bridge, fedoraOp);
        solrOp.indexDoc(new ByteArrayInputStream(foxml), "0");
        
        //        XPathFactory factory = XPathFactory.newInstance();
//        XPath  xpath = factory.newXPath();
//        XPathExpression expr = xpath.compile("//datastream/datastreamVersion[last()]/xmlContent/RDF");
//        NodeList nlist = (NodeList) expr.evaluate(parseDocument, XPathConstants.NODESET);
//        System.out.println(nlist.getLength());
//        for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
//            System.out.println(nlist.item(i).getNodeName());
//        }
        //System.out.println(parseDocument);
    }
}
