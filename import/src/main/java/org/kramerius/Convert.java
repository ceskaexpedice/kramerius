package org.kramerius;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.apache.solr.client.solrj.SolrServerException;
import org.xml.sax.SAXException;

import com.qbizm.kramerius.imptool.poc.Main;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class Convert {

    /**
     * @param args[0] visibility (true, false)
     * @throws UnsupportedEncodingException 
     */
    public static void main(String[] args) throws InterruptedException, JAXBException, IOException, SAXException, ServiceException, RepositoryException, SolrServerException {
        String convertTargetDirectory = System.getProperties().containsKey("convert.target.directory") ? System.getProperty("convert.target.directory") : KConfiguration.getInstance().getProperty("convert.target.directory") ;
        String defaultRights = System.getProperties().containsKey("convert.defaultRights") ?  System.getProperty("convert.defaultRights") : KConfiguration.getInstance().getProperty("convert.defaultRights","false") ;
        String convertDirectory =  System.getProperties().containsKey("convert.directory") ? System.getProperty("convert.directory") : KConfiguration.getInstance().getProperty("convert.directory");
        
        boolean visible = Boolean.parseBoolean(defaultRights);
        if (args.length>0){
            visible = Boolean.parseBoolean(args[0]);
        }
        String uuid = Main.convert(convertDirectory, convertTargetDirectory, false, visible, null);

        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule(),new ImportModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Import.ingest(fa, feeder, KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), convertTargetDirectory);

        /*if (!KConfiguration.getInstance().getConfiguration().getBoolean("ingest.skip",false)){
            Download.startIndexing("converted", uuid);
        }*/
    }

}
