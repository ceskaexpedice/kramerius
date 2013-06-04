package cz.incad.kramerius.processes.impl;

import static cz.incad.kramerius.utils.IOUtils.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;


import cz.incad.kramerius.Constants;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class LRProcessDefinitionManagerImpl implements DefinitionManager {


	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(LRProcessDefinitionManagerImpl.class.getName());
	private KConfiguration configuration;
	private LRProcessManager processManager;

	
	@Inject
	public LRProcessDefinitionManagerImpl(KConfiguration configuration,
			LRProcessManager processManager, 
			@Named("LIBS")String defaultLibsdir//, 
			/*String configFile*/) {
		super();
		this.configuration = configuration;
		this.processManager = processManager;
		LOGGER.fine("loading configuration ...");
		this.load();
		
	}


	private HashMap<String, LRProcessDefinition> definitions = new HashMap<String, LRProcessDefinition>();


	@Override
	public LRProcessDefinition getLongRunningProcessDefinition(String id) {
		return definitions.get(id);
	}


	@Override
	public synchronized void load() {
		try {
			File defaultWorkDir = new File(DEFAULT_LP_WORKDIR);
			if (!defaultWorkDir.exists()) {
				boolean created = defaultWorkDir.mkdirs();
				if (!created) throw new RuntimeException("cannot create directory '"+defaultWorkDir+"'");
			}
			
			LOGGER.fine("Loading configuration from jar ");
			byte[] bytes = defaultLPXML().getBytes(Charset.forName("UTF-8"));
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			try {
				loadFromStream(bis);
			} finally {
				if (bis !=null) bis.close();
			}
			
			
			String parsingSource = CONFIGURATION_FILE;
			File file = new File(parsingSource);
			if (file.exists()) {
				LOGGER.fine("Loading file from '"+CONFIGURATION_FILE+"'");
				FileInputStream fis = new FileInputStream(file);
				try {
					loadFromStream(fis);
				} finally {
					if (fis !=null) fis.close();
				}
			}
			
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}


	private String defaultLPXML() throws Exception{
		StringTemplateGroup grp = new StringTemplateGroup("m");
		StringTemplate template = grp.getInstanceOf("cz/incad/kramerius/processes/res/lp");
		template.setAttribute("user_home", System.getProperties().getProperty("user.home"));
		template.setAttribute("default_lp_work_dir", DEFAULT_LP_WORKDIR);
		String string = template.toString();
		return string;
	}


	private void loadFromStream(InputStream fis)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document parsed = builder.parse(fis);
		NodeList childNodes = parsed.getDocumentElement().getChildNodes();
		for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				LRProcessDefinitionImpl def = new LRProcessDefinitionImpl(this.processManager, this.configuration);
				def.loadFromXml((Element) item);
				this.definitions.put(def.getId(), def);
			}
		}
	}

	
	@Override
	public List<LRProcessDefinition> getLongRunningProcessDefinitions() {
		return new ArrayList<LRProcessDefinition>(definitions.values());
	}


	public KConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(KConfiguration configuration) {
		this.configuration = configuration;
	}


	public LRProcessManager getProcessManager() {
		return processManager;
	}


	public void setProcessManager(LRProcessManager processManager) {
		this.processManager = processManager;
	}

	public static void main(String[] args) {
		//LRProcessDefinitionManagerImpl impl = new LRProcessDefinitionManagerImpl(KConfiguration.getKConfiguration(), null);
	}
}
