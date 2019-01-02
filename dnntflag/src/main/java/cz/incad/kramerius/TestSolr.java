package cz.incad.kramerius;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.services.MigrationUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestSolr {

    public static void main(String[] args) throws IOException, TransformerException, InterruptedException, MigrateSolrIndexException, BrokenBarrierException, SAXException, ParserConfigurationException {
        Client client = Client.create();
        //all(client);
        parent(client);
        return;
    }

    private static void all( Client client) throws ParserConfigurationException, MigrateSolrIndexException, SAXException, IOException, InterruptedException, BrokenBarrierException {
        final List<String> all = new ArrayList<>();
        IterationUtils.cursorIteration(client, MigrationUtils.configuredSourceServer(),"*:*",(em, i) -> {
            List<String> pp = MigrationUtils.findAllPids(em);
            System.out.println(pp);
            all.addAll(pp);

        }, ()->{
            System.out.println("SIZE:"+all.size());
        });
        System.out.println("TTT:"+all);
    }

    private static void parent(Client client) throws ParserConfigurationException, MigrateSolrIndexException, SAXException, IOException, InterruptedException, BrokenBarrierException {
        final List<String> all = new ArrayList<>();
        IterationUtils.cursorIteration(client, MigrationUtils.configuredSourceServer(), URLEncoder.encode("root_pid:\"uuid:df196150-64dd-11e4-b42a-005056827e52\"","UTF-8"),(em, i) -> {
            List<String> pp = MigrationUtils.findAllPids(em);
            System.out.println(pp);
            all.addAll(pp);

        }, ()->{
            System.out.println("SIZE:"+all.size());
        });
        System.out.println("TTT:"+all);

    }

}
