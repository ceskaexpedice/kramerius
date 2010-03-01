package cz.incad.kramerius.processes.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;


import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class LRProcessDefinitionManagerImpl implements DefinitionManager {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(LRProcessDefinitionManagerImpl.class.getName());
	
	@Inject
	private KConfiguration configuration;
	@Inject
	private LRProcessManager processManager;
	
	
	
	private HashMap<String, LRProcessDefinition> definitions = new HashMap<String, LRProcessDefinition>();


	@Override
	public LRProcessDefinition getLongRunningProcessDefinition(String id) {
		return definitions.get(id);
	}


	@Override
	public void load() {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document parsed = builder.parse(this.configuration.getLongRunningProcessDefiniton());
			NodeList childNodes = parsed.getDocumentElement().getChildNodes();
			for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
				Node item = childNodes.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					LRProcessDefinitionImpl def = new LRProcessDefinitionImpl(this.processManager, this.configuration);
					def.loadFromXml((Element) item);
					this.definitions.put(def.getId(), def);
				}
			}
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException
				
				
				e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
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

	
}
