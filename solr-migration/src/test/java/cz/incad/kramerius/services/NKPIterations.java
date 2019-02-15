package cz.incad.kramerius.services;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;

public class NKPIterations {

    public static void main(String[] args) throws InterruptedException, ParserConfigurationException, IOException, BrokenBarrierException, SAXException, MigrateSolrIndexException {
        final List<Integer> list = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        Client client = Client.create();
        IterationUtils.queryPaginationIteration(client, "http://10.10.0.94:8080/solr/","*:*", (Element element, String t) ->{
            List<String> allPids = MigrationUtils.findAllPids(element);
            list.add(allPids.size());
            int sum = sum(list);
            System.out.println(sum(list));
        },() ->{
            System.out.println("Konec");
            long stopTime = System.currentTimeMillis();
            System.out.println("It took "+(stopTime - startTime));
        });
    }

    public static final int sum(List<Integer> sum) {
        return sum.stream().mapToInt(Integer::intValue).sum();
    }
}
