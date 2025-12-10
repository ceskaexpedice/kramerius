package cz.incad.kramerius.processes.definition;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessDefinitionManagerImpl implements ProcessDefinitionManager {

    public static final Logger LOGGER = Logger.getLogger(ProcessDefinitionManagerImpl.class.getName());
    private KConfiguration configuration = KConfiguration.getInstance();

    @Inject
    public ProcessDefinitionManagerImpl() {
        super();
        LOGGER.fine("loading configuration ...");
        this.load();
    }

    private HashMap<String, ProcessDefinition> definitions = new HashMap<String, ProcessDefinition>();

    @Override
    public ProcessDefinition getProcessDefinition(String id) {
        return definitions.get(id);
    }

    @Override
    public synchronized void load() {
        try {
            File defaultWorkDir = new File(DEFAULT_LP_WORKDIR);
            if (!defaultWorkDir.exists()) {
                boolean created = defaultWorkDir.mkdirs();
                if (!created)
                    throw new RuntimeException("cannot create directory '" + defaultWorkDir + "'");
            }

            LOGGER.fine("Loading configuration from jar ");
            byte[] bytes = defaultLPXML().getBytes(Charset.forName("UTF-8"));
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            try {
                loadFromStream(bis);
            } finally {
                if (bis != null)
                    bis.close();
            }

            String parsingSource = CONFIGURATION_FILE;
            File file = new File(parsingSource);
            if (file.exists()) {
                LOGGER.fine("Loading file from '" + CONFIGURATION_FILE + "'");
                FileInputStream fis = new FileInputStream(file);
                try {
                    loadFromStream(fis);
                } finally {
                    if (fis != null)
                        fis.close();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private String defaultLPXML() {
        StringTemplateGroup grp = new StringTemplateGroup("m");
        StringTemplate template = grp.getInstanceOf("cz/incad/kramerius/processes/res/lp");
        template.setAttribute("user_home", System.getProperties().getProperty("user.home"));
        template.setAttribute("default_lp_work_dir", DEFAULT_LP_WORKDIR);
        String string = template.toString();
        return string;
    }

    private void loadFromStream(InputStream fis) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document parsed = builder.parse(fis);
        NodeList childNodes = parsed.getDocumentElement().getChildNodes();
        for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                ProcessDefinitionImpl def = new ProcessDefinitionImpl();
                def.loadFromXml((Element) item);
                this.definitions.put(def.getId(), def);
            }
        }
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        return new ArrayList<ProcessDefinition>(definitions.values());
    }

}
