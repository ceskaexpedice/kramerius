package cz.incad.kramerius.processes.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.stringtemplate.StringTemplate;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class IterateNonParametrized {

    public static RepositoryItemsSupport repoItems;
    
    public static void main(String[] args) throws JSONException {
        if (args.length == 2) {
            // first is definiton
            String def = args[0];
            //second is searialized parameters to subproesses
            String serializedFormOfArguments = args[1];

            List<String> topLevelModels = KConfiguration.getInstance().getConfiguration().getList("fedora.topLevelModels");
            RepositoryItemsSupport repoItems = new SolrRepoItemsSupport("PID");
            for (String m : topLevelModels) {
                List<String> pids = repoItems.findPidsByModel(m);
                for (int i = 0; i < pids.size(); i++) {
                    String p = pids.get(i);
                    ArrayList<String> alist = new ArrayList<String>();
                    String[] subProcsArgs = deserializeArguments(serializedFormOfArguments);
                    for (int j = 0; j < subProcsArgs.length; j++) {
                        subProcsArgs[j]= template(subProcsArgs[j],p,""+i, m);
                    }
                    ProcessUtils.startProcess(def, subProcsArgs);
                }
            }
        }
    }

    public static String template(String arg, String p, String index, String m) {
        StringTemplate template = new StringTemplate(arg);
        template.setAttribute("pid", p);
        template.setAttribute("index", index);
        template.setAttribute("model", m);
        return template.toString();
    }


    public static String[] deserializeArguments(String serializedFormOfArguments) throws JSONException {
        List<String> astring = new ArrayList<String>();
        JSONArray jsonArray = new JSONArray(serializedFormOfArguments);
        for (int i = 0, ll = jsonArray.length(); i < ll; i++) {
            Object obj = jsonArray.get(i);
            if (obj != null) {
                astring.add(obj.toString());
            }
        }
        return astring.toArray(new String[astring.size()]);
    }


    public static class SolrRepoItemsSupport implements RepositoryItemsSupport {
        
        public static final Logger LOGGER = Logger.getLogger(SolrRepoItemsSupport.class.getName());
        
        private SolrAccess solrAccess = null;
        private List<String> fList = new ArrayList<String>();
        
        public SolrRepoItemsSupport(String ... flist) {
            super();
            this.solrAccess = new SolrAccessImpl();
            this.fList = Arrays.asList(flist);
        }

        @Override
        public List<String> findPidsByModel(String model) {
            List<String> pids = new  ArrayList<String>();
            try {
                int rows = 10000;
                int size = 1; // 1 for the first iteration
                int offset = 0;
                while (offset < size) {
                    // request
                    String request = "q=fedora.model:\"" + model
                            + "\"&rows=" + rows + "&start=" + offset;
                    if (!fList.isEmpty()) {
                        request+="&fl=";
                        for (int i = 0,bl=fList.size(); i < bl; i++) {
                            if (i >= 0) request += ",";
                            request += fList.get(i);
                        }
                    }
                    
                    Document resp = solrAccess.request(request);
                    Element resultelm = XMLUtils.findElement(resp.getDocumentElement(), "result");
                    // define size
                    size = Integer.parseInt(resultelm.getAttribute("numFound"));
                    List<Element> elms = XMLUtils.getElements(resultelm,
                            new XMLUtils.ElementsFilter() {
                                @Override
                                public boolean acceptElement(Element element) {
                                    if (element.getNodeName().equals("doc")) {
                                        return true;
                                    } else
                                        return false;
                                }
                            });
                    
                    for (Element docelm : elms) {
                        Element element = XMLUtils.findElement(docelm, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String name = element.getNodeName();
                                String attribute = element.getAttribute("name");
                                return ((name.equals("str") && attribute.equals("PID")));
                            }
                        });
                        if (element != null) {
                            String pidContent = element.getTextContent();
                            pids.add(pidContent);
                        }
                    }
                    offset = offset + rows;
                }
                return pids;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (DOMException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
            return pids;
        }

    }
}
