package cz.incad.kramerius;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.services.MigrationUtils;
import cz.incad.kramerius.solr.SolrFieldsMapping;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;
import cz.incad.kramerius.workers.DNNTLabelWorker;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TestUrl {

    public static final Logger LOGGER = Logger.getLogger(TestUrl.class.getName());

    static Set<String> fetchAllPids(String q) throws UnsupportedEncodingException {
        Client client =  Client.create();
        //String masterQuery = URLEncoder.encode(q,"UTF-8");
        Set<String> allSet = new HashSet<>();
        if (true) {
            try {
                IterationUtils.cursorIteration(client, KConfiguration.getInstance().getSolrSearchHost() ,q,(em, i) -> {
                    List<String> pp = MigrationUtils.findAllPids(em);
                    allSet.addAll(pp);
                }, ()->{});
            } catch (ParserConfigurationException | SAXException | IOException | InterruptedException | MigrateSolrIndexException | BrokenBarrierException e  ) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }


        } else try {
            IterationUtils.queryFilterIteration(client, MigrationUtils.configuredSourceServer(), q, (em, i) -> {
                List<String> pp = MigrationUtils.findAllPids(em);
                allSet.addAll(pp);
            }, () -> {
            });
        } catch (MigrateSolrIndexException | IOException | SAXException | ParserConfigurationException | BrokenBarrierException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return allSet;
    }


    protected static String solrChildrenQuery(List<String> pidPaths, String label, boolean addRemoveFlag) {
        // it encoding
        String pidPathQuery =  SolrFieldsMapping.getInstance().getPidPathField()+":("+pidPaths.stream().map(SolrUtils::escapeQuery).map(it-> it+"*").collect(Collectors.joining(" OR "))+")";
//        return addRemoveFlag ?
//                KConfiguration.getInstance().getConfiguration().getString( DNNTLabelWorker.DNNT_LABEL_QUERY,"("+pidPathQuery+ " -" + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) ")  :
//                KConfiguration.getInstance().getConfiguration().getString( DNNTLabelWorker.DNNT_LABEL_QUERY_UNSET,"("+pidPathQuery+ " " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":\"" +label+"\")");
//

        return addRemoveFlag ?
                KConfiguration.getInstance().getConfiguration().getString(  DNNTLabelWorker.DNNT_LABEL_QUERY,"("+pidPathQuery+ " -" + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " NOT " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":\"" +label+"\")")  :
                KConfiguration.getInstance().getConfiguration().getString( DNNTLabelWorker.DNNT_LABEL_QUERY_UNSET,"("+pidPathQuery+ " " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":\"" +label+"\")");

    }



    public static void main(String[] args) throws IOException {
        //List<String> strings = Arrays.asList("uuid:045b1250-7e47-11e0-add1-000d606f5dc6/uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6/uuid:91214030-80bb-11e0-b482-000d606f5dc6/uuid:00dbc770-8138-11e0-b63f-000d606f5dc6");
        List<String> strings = Arrays.asList("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
        String s = solrChildrenQuery(strings, "dnnto", true);
        s = URLEncoder.encode(s,"UTF-8");

        Set<String> strings1 = fetchAllPids(s);
        System.out.println(strings1.size());
        System.out.println(s);


    }

}
