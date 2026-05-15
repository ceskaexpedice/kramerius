package cz.incad.kramerius;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import nl.siegmann.epublib.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static cz.incad.kramerius.utils.IterationUtils.getSortField;
import static cz.incad.kramerius.utils.IterationUtils.useCompositeId;

public class ProcessHelper {

    private static final Logger LOGGER = Logger.getLogger(ProcessHelper.class.getName());

    //FIXME: duplicate code (same method in NewIndexerProcessIndexObject, SetPolicyProcess), use abstract/utility class, but not before bigger cleanup in process scheduling
    //["Quartet A minor", " op. 51", " no. 2. Andante moderato"] => "Quartet A minor, op. 51, no. 2 Andante moderato"
    static String mergeArraysEnd(String[] args, int argsIndex) {
        String result = "";
        for (int i = argsIndex; i < args.length; i++) {
            String arg = args[i];
            if (arg != null && !"null".equals(arg)) {
                result += args[i];
                if (i != args.length - 1) {
                    result += ",";
                }
            }
        }
        result = result.trim();
        return result.isEmpty() ? null : result;
    }

    static String shortenIfTooLong(String string, int maxLength) {
        if (string == null || string.isEmpty() || string.length() <= maxLength) {
            return string;
        } else {
            String suffix = "...";
            return string.substring(0, maxLength - suffix.length()) + suffix;
        }
    }

    /**
     * Vraci PIDy všech potomku v davkach
     */
    public static class EffectivePidsOfDescendantsProducer implements Iterator<List<String>> {
        private static final int BATCH_SIZE = 1000;

        private final SolrAccess searchIndex;
        private final String q;
        private String cursorMark = null;
        private String nextCursorMark = "*";
        private int total = 0;
        private int returned = 0;

        public EffectivePidsOfDescendantsProducer(String targetPid, SolrAccess searchIndex, boolean onlyOwnDescendants) {
            this.searchIndex = searchIndex;
            String ownPidPath = null;
            List<String> pidsPaths = null;
            try {
                Document doc = this.searchIndex.getSolrDataByPid(targetPid, "own_pid_path,pid_paths");
                Element oPPE = XMLUtils.findElement(doc.getDocumentElement(), (element) -> {
                    if (element.hasAttribute("name") && element.getAttribute("name").equals("own_pid_path")) {
                        return true;
                    } else return false;
                });
                if (oPPE != null) {
                    ownPidPath = oPPE.getTextContent();
                }
                Element ppE = XMLUtils.findElement(doc.getDocumentElement(), (element) -> {
                    if (element.hasAttribute("name") && element.getAttribute("name").equals("pid_paths")) {
                        return true;
                    } else return false;
                });

                if (ppE  != null && ppE.getNodeName().equals("arr")) {
                    List<String> collections = XMLUtils.getElements(ppE, (elm) -> {
                        return elm.getNodeName().equals("str");
                    }).stream().map(Element::getTextContent).collect(Collectors.toList());

                    pidsPaths = collections;
                } else {
                    pidsPaths = new ArrayList<>();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (onlyOwnDescendants) {
                if (StringUtils.isAnyString(ownPidPath)) {
                    this.q =  String.format("own_pid_path.children:\"%s\"", ownPidPath);
                } else throw new  RuntimeException("own_pid_path.children:\"not found");
            } else {
                if (pidsPaths.size() > 0) {
                    String orQ = pidsPaths.stream().map(it-> {
                        return "pid_paths.children:\"" + it + "\"";
                    }).collect(Collectors.joining(" OR "));
                    this.q =  orQ;
                } else throw new  RuntimeException("own_pid_path.children:\"not found");
            }
        }

        public int getTotal() {
            return total;
        }

        public int getReturned() {
            return returned;
        }


        @Override
        public boolean hasNext() {
            if (total > 0 && returned == total) {
                return false;
            } else {//obrana proti zacykleni, kdyby se solr zachoval divne a nektery slibeny objekt nevratil
                return !nextCursorMark.equals(cursorMark);
            }
        }

        public String getQ() {
            return q;
        }

        @Override
        public List<String> next() {
            try {
                List<String> result = new ArrayList<>();
                cursorMark = nextCursorMark;
                String query = String.format("fl=pid&sort="+getSortField()+"+asc&rows=%d&q=%s&cursorMark=%s", BATCH_SIZE, URLEncoder.encode(q, "UTF-8"), cursorMark);
                JSONObject jsonObject = searchIndex.requestWithSelectReturningJson(query, null);
                JSONObject response = jsonObject.getJSONObject("response");
                nextCursorMark = jsonObject.getString("nextCursorMark");
                total = response.getInt("numFound");
                if (total != 0) {
                    JSONArray docs = response.getJSONArray("docs");
                    for (int i = 0; i < docs.length(); i++) {
                        String pid = docs.getJSONObject(i).getString("pid");
                        result.add(pid);
                    }
                }
                returned += result.size();
                return result;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



    /**
     * Vraci PIDy všech potomku v davkach
     */
    public static class PidsOfDescendantsProducer implements Iterator<List<String>> {
        private static final int BATCH_SIZE = 1000;

        private final SolrAccess searchIndex;
        private final String q;
        private String cursorMark = null;
        private String nextCursorMark = "*";
        private int total = 0;
        private int returned = 0;

        public PidsOfDescendantsProducer(String targetPid, SolrAccess searchIndex, boolean onlyOwnDescendants) {
            this.searchIndex = searchIndex;
            String pidEscaped = targetPid.replace(":", "\\:");
            this.q = onlyOwnDescendants
                    ? String.format("own_pid_path:%s/* OR own_pid_path:*/%s/* ", pidEscaped, pidEscaped)
                    : String.format("pid_paths:%s/* OR pid_paths:*/%s/* ", pidEscaped, pidEscaped);
        }

        public int getTotal() {
            return total;
        }

        public int getReturned() {
            return returned;
        }

        public String getQ() {
            return q;
        }

        @Override
        public boolean hasNext() {
            if (total > 0 && returned == total) {
                return false;
            } else {//obrana proti zacykleni, kdyby se solr zachoval divne a nektery slibeny objekt nevratil
                return !nextCursorMark.equals(cursorMark);
            }
        }

        @Override
        public List<String> next() {
            try {
                List<String> result = new ArrayList<>();
                cursorMark = nextCursorMark;
                String query = String.format("fl=pid&sort="+getSortField()+"+asc&rows=%d&q=%s&cursorMark=%s", BATCH_SIZE, URLEncoder.encode(q, "UTF-8"), cursorMark);
                JSONObject jsonObject = searchIndex.requestWithSelectReturningJson(query, null);
                JSONObject response = jsonObject.getJSONObject("response");
                nextCursorMark = jsonObject.getString("nextCursorMark");
                total = response.getInt("numFound");
                if (total != 0) {
                    JSONArray docs = response.getJSONArray("docs");
                    for (int i = 0; i < docs.length(); i++) {
                        String pid = docs.getJSONObject(i).getString("pid");
                        result.add(pid);
                    }
                }
                returned += result.size();
                return result;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
