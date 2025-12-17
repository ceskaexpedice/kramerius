package cz.inovatika.licenses;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.security.licenses.impl.embedded.cz.CzechEmbeddedLicenses;
import cz.incad.kramerius.utils.IterationUtils;
import cz.incad.kramerius.utils.IterationUtils.Endpoint;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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
        // Configuraed all top level models 
        List<String> models = KConfiguration.getInstance().getConfiguration().getList(PROCESSES_CONF_KEY + "models", DEFAULT_MODELS).stream().map(Objects::toString).collect(Collectors.toList());

        // don't use periodical
        models.remove("periodical");

        String query = String.format("model:(%s) AND accessibility:*", models.stream().collect(Collectors.joining(" OR ")));
        String encodedQ = URLEncoder.encode(query, "UTF-8");

        Client client = Client.create();

        Map<String, String> topLevelModelsMap = new HashMap<>();
        Map<String, List<Pair<String, String>>> subLevelModelsMap = new HashMap<>();
        Map<String, Triple<String, String, String>> details = new HashMap<>();

        List<String> alreadyLicensedPids = new ArrayList<>();

        List<String> publicPids = new ArrayList<>();
        List<String> privatePids = new ArrayList<>();


        IterationUtils.cursorIteration(new IterationUtils.FieldsProvider(getSortField(), "pid", "root.pid", "accessibility", "model", "licenses"), Endpoint.select, client, KConfiguration.getInstance().getSolrSearchHost(), encodedQ, (elm, iter) -> {
            try {
                List<Element> docs = XMLUtils.getElementsRecursive(elm, new XMLUtils.ElementsFilter() {

                    @Override
                    public boolean acceptElement(Element element) {
                        return element.getNodeName().equals("doc");
                    }
                });


                docs.stream().forEach(doc -> {


                    AtomicReference<String> accessibility = new AtomicReference<>();
                    AtomicReference<String> model = new AtomicReference<>();
                    AtomicReference<String> pid = new AtomicReference<>();
                    AtomicReference<String> rootPid = new AtomicReference<>();

                    AtomicReference<List<String>> lics = new AtomicReference<>();

                    XMLUtils.getElements(doc).forEach(e -> {
                        String name = e.getAttribute("name");
                        switch (name) {
                            case "accessibility":
                                accessibility.set(e.getTextContent());
                                break;
                            case "model":
                                model.set(e.getTextContent());
                                break;
                            case "pid":
                                pid.set(e.getTextContent());
                                break;
                            case "root.pid":
                                rootPid.set(e.getTextContent());
                                break;
                            case "licenses":
                                List<Element> elements = XMLUtils.getElements(e);
                                List<String> collectedLicenses = elements.stream().map(Element::getTextContent).collect(Collectors.toList());
                                lics.set(collectedLicenses);
                                break;
                        }
                    });

                    if (model.get() != null && models.contains(model.get())) {
                        topLevelModelsMap.put(pid.get(), accessibility.get());
                    } else {
                        if (!topLevelModelsMap.containsKey(rootPid.get())) {
                            topLevelModelsMap.put(rootPid.get(), "");
                        }
                        if (!subLevelModelsMap.containsKey(rootPid.get())) {
                            subLevelModelsMap.put(rootPid.get(), new ArrayList<>());
                        }
                        List<Pair<String, String>> list = subLevelModelsMap.get(rootPid.get());
                        list.add(Pair.of(pid.get(), accessibility.get()));
                    }
                    details.put(pid.get(), Triple.of(model.get(), accessibility.get(), pid.get()));


                    if (lics.get() != null && (lics.get().contains(CzechEmbeddedLicenses.PUBLIC_LICENSE.getName()) || lics.get().contains(CzechEmbeddedLicenses.ONSITE_LICENSE.getName()))) {
                        alreadyLicensedPids.add(pid.get());
                    }
                });

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }, null);

        topLevelModelsMap.keySet().forEach(rootPid -> {
            if (!subLevelModelsMap.containsKey(rootPid)) {
                if (topLevelModelsMap.get(rootPid).equals("public")) {
                    publicPids.add(rootPid);
                } else {
                    privatePids.add(rootPid);
                }

            } else {
                subLevelModelsMap.get(rootPid).stream().forEach(p -> {
                    if (p.getRight().equals("public")) {
                        publicPids.add(p.getLeft());
                    } else {
                        privatePids.add(p.getLeft());
                    }
                });
            }
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

}
