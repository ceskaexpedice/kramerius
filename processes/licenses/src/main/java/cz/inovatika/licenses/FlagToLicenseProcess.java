package cz.inovatika.licenses;

import cz.incad.kramerius.security.licenses.impl.embedded.cz.CzechEmbeddedLicenses;
import cz.incad.kramerius.utils.IterationUtils;
import cz.incad.kramerius.utils.IterationUtils.Endpoint;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.kramerius.services.iterators.ProcessIterator;
import cz.inovatika.kramerius.services.iterators.ProcessIteratorFactory;
import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;
import cz.inovatika.kramerius.services.iterators.factories.SolrIteratorFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.ceskaexpedice.processplatform.common.model.ScheduleSubProcess;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static cz.incad.kramerius.utils.IterationUtils.getSortField;

/**
 * Transforming accesibility flag to license
 * <ul>
 *  <li> policy:private -&gt; onsite </li>
 *  <li> policy:public -&gt; public </li>
 * </ul>
 *
 * @author happy
 */
public class FlagToLicenseProcess {

    public static final Logger LOGGER = Logger.getLogger(FlagToLicenseProcess.class.getName());

    // Default configuration prefix key
    public static final String PROCESSES_CONF_KEY = "processess.flag_to_license.";


    public static final int DEFAULT_BATCH_SIZE = KConfiguration.getInstance().getConfiguration().getInt("flagToLicense", 1000);

    public static final List<String> DEFAULT_MODELS = Arrays.asList(
            "monograph",
            "monographunit",
            "periodicalvolume",
            "manuscript",
            "soundrecording",
            "convolute",
            "map",
            "sheetmusic",
            "graphic",
            "archive",
            "convolute");

    public enum Scope {
        OBJECT, TREE
    }


    /**
     * Iterates over search index and add public or onsite licenses according accessibility flag
     *
     * @throws BrokenBarrierException
     * @throws InterruptedException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static void main() throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            List<String> models = KConfiguration.getInstance().getConfiguration().getList(PROCESSES_CONF_KEY + "models", DEFAULT_MODELS).stream().map(Objects::toString).collect(Collectors.toList());

            // don't use periodical
            models.remove("periodical");

            String query = String.format("model:(%s) AND accessibility:*", models.stream().collect(Collectors.joining(" OR ")));
            //String encodedQ = URLEncoder.encode(query, "UTF-8");

            //Client client = Client.create();

            //Map<String, String> topLevelModelsMap = new HashMap<>();
            //Map<String, List<Pair<String, String>>> subLevelModelsMap = new HashMap<>();
            //Map<String, Triple<String, String, String>> details = new HashMap<>();

            List<String> alreadyLicensedPids = new ArrayList<>();

            List<String> publicPids = new ArrayList<>();
            List<String> privatePids = new ArrayList<>();

            //SolrIteratorConfig.Builder builder = new SolrIteratorConfig.Builder(url, id);

            SolrIteratorConfig config =
                    new SolrIteratorConfig.Builder(KConfiguration.getInstance().getSolrSearchHost(), "pid")
                            .fieldList("pid,root.pid,accessibility,model,licenses,count_monograph_unit")
                            .sort("pid asc")
                            .endpoint("select")
                            .filterQuery(query)
                            .factoryClz(SolrIteratorFactory.class.getName())
                            .build();
            ProcessIteratorFactory iteratorFactory = ProcessIteratorFactory.create(config);
            ProcessIterator processIterator = iteratorFactory.createProcessIterator(config, httpClient);
            processIterator.iterate(httpClient, (itdocs) -> {
                itdocs.forEach(doc -> {
                    String pid = doc.getPid();

                    String accessibility = doc.getDoc().get("accessibility").toString();
                    String model = doc.getDoc().get("model").toString();
                    String rootPid = doc.getDoc().get("root.pid").toString();
                    Object countMonographUnit = doc.getDoc().get("count_monograph_unit");
                    List<String> lics = (List<String>) doc.getDoc().get("licenses");

                    // pridavame pro monografie, ktere nemaji count_monograph_unit
                    if (model.equals("monograph") &&  countMonographUnit == null) {
                        if (accessibility.equals("public")) {
                            publicPids.add(pid);
                        } else if (accessibility.equals("private")) {
                            privatePids.add(pid);
                        }
                    }
                    if (model.equals("periodicalvolume") || model.equals("monographunit")) {
                        if (accessibility.equals("public")) {
                            publicPids.add(pid);
                        } else if (accessibility.equals("private")) {
                            privatePids.add(pid);
                        }
                    }



                    if (lics != null && (lics.contains(CzechEmbeddedLicenses.PUBLIC_LICENSE.getName()) || lics.contains(CzechEmbeddedLicenses.ONSITE_LICENSE.getName()))) {
                        alreadyLicensedPids.add(pid);
                    }

//                    if (accessibility != null && accessibility.equals("public")) {
//                        publicPids.add(pid);
//                    } else if (accessibility != null && accessibility.equals("private")) {
//                        privatePids.add(pid);
//                    }

                });
            }, () -> {

            });


            // remove from public list & private list
            alreadyLicensedPids.forEach(pid -> {
                publicPids.remove(pid);
                privatePids.remove(pid);
            });


            LOGGER.info(String.format("Number of already licensed pids: %d", alreadyLicensedPids.size()));
            LOGGER.info(String.format("To public license: %d", publicPids.size()));
            LOGGER.info(String.format("To onsite license: %d", privatePids.size()));

            // public batches
            scheduleSetLicenses(publicPids, CzechEmbeddedLicenses.PUBLIC_LICENSE.getName());

            // private batches
            scheduleSetLicenses(privatePids, CzechEmbeddedLicenses.ONSITE_LICENSE.getName());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void scheduleSetLicenses(List<String> pids, String lic) throws JSONException {
        int iterations = pids.size() / DEFAULT_BATCH_SIZE + (pids.size() % DEFAULT_BATCH_SIZE == 0 ? 0 : 1);

        for (int i = 0; i < iterations; i++) {
            int start = i * DEFAULT_BATCH_SIZE;
            int end = Math.min((i + 1) * DEFAULT_BATCH_SIZE, pids.size());

            List<String> batch = pids.subList(start, end);
            String pidlistArgument = "pidlist:" + String.join(";", batch);

            Map<String, String> payload = new HashMap<>();
            payload.put("license", lic);
            payload.put("pid", pidlistArgument);

            ScheduleSubProcess subProcess = new ScheduleSubProcess("add_license", payload);

            PluginContext pluginContext = PluginContextHolder.getContext();
            pluginContext.scheduleSubProcess(subProcess);
        }
    }

    public static void main(String[] args) throws Exception {
        main();
    }

}
