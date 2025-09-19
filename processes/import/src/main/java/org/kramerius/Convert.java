package org.kramerius;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.kramerius.importer.inventory.ScheduleStrategy;
import org.xml.sax.SAXException;

import com.qbizm.kramerius.imptool.poc.Main;
import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class Convert {

    /**
     * @param args[0] visibility (true, false)
     * @throws UnsupportedEncodingException 
     */
    public static void main(String[] args) throws InterruptedException, JAXBException, IOException, SAXException, ServiceException, SolrServerException {
        String convertTargetDirectory = System.getProperties().containsKey("convert.target.directory") ? System.getProperty("convert.target.directory") : KConfiguration.getInstance().getProperty("convert.target.directory") ;
        String defaultRights = System.getProperties().containsKey("convert.defaultRights") ?  System.getProperty("convert.defaultRights") : KConfiguration.getInstance().getProperty("convert.defaultRights","false") ;
        String convertDirectory =  System.getProperties().containsKey("convert.directory") ? System.getProperty("convert.directory") : KConfiguration.getInstance().getProperty("convert.directory");
        
        boolean visible = Boolean.parseBoolean(defaultRights);
        if (args.length>0){
            visible = Boolean.parseBoolean(args[0]);
        }
        String uuid = Main.convert(convertDirectory, convertTargetDirectory, false, visible, null);

        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule(),new ImportModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        SortingService sortingServiceLocal = injector.getInstance(SortingService.class);
        try {
            Import.run(akubraRepository, akubraRepository.pi(), sortingServiceLocal, KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), convertTargetDirectory, ScheduleStrategy.indexRoots);
        }finally {
            akubraRepository.shutdown();
        }

        /*if (!KConfiguration.getInstance().getConfiguration().getBoolean("ingest.skip",false)){
            Download.startIndexing("converted", uuid);
        }*/
    }

}
