package cz.incad.kramerius.processes.impl;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLEngineResult.Status;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.os.impl.windows.WindowsLRProcessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class LRProcessDefinitionImpl implements LRProcessDefinition {

	public static final String DEFAULT_LIB_DIR="lib";
	public static final String DEFAULT_USER_DIR ="";
	
	private String libsDir = DEFAULT_LIB_DIR;
	private String id;
	private String mainClass;
	private String description;
	private String standardStreamFolder;
	private String errStreamFolder;
	
	private List<String> parameters = new ArrayList<String>();
	
	private LRProcessManager pm;
	private KConfiguration configuration;
	
	public LRProcessDefinitionImpl(LRProcessManager pm, KConfiguration configuration) {
		super();
		this.pm = pm;
		this.configuration = configuration;
	}

	@Override
	public String getLibsDir() {
		return this.libsDir;
	}

	@Override
	public List<String> getParameters() {
		return new ArrayList<String>(this.parameters);
	}


	public void loadFromXml(Element elm) {
		NodeList nodes = elm.getChildNodes();
		for (int i = 0,ll=nodes.getLength(); i < ll; i++) {
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = item.getNodeName();
				if (nodeName.equals("id")) {
					this.id = item.getTextContent();
				}
				if (nodeName.equals("mainClass")) {
					this.mainClass = item.getTextContent();
				}
				if (nodeName.equals("description")) {
					this.description = item.getTextContent();
				}
				if (nodeName.equals("lib")) {
					this.libsDir = item.getTextContent();
				}
				if (nodeName.equals("standardOs")) {
					this.standardStreamFolder = item.getTextContent();
				}
				if (nodeName.equals("errOs")) {
					this.errStreamFolder = item.getTextContent();
				}
				if (nodeName.equals("parameters")) {
					parameters(item);
				}
			}
		}
	}

	private void parameters(Node item) {
		Element elm = (Element) item;
		NodeList nodes = elm.getChildNodes();
		for (int i = 0,ll=nodes.getLength(); i < ll; i++) {
			Node chItem = nodes.item(i);
			if (chItem.getNodeType() == Node.ELEMENT_NODE) {
				String chItemName = chItem.getNodeName();
				if (chItemName.equals("parameter")) {
					String chItemVal = chItem.getTextContent();
					this.parameters.add(chItemVal);
				}
			}
		}		
	}

	@Override
	public LRProcess createNewProcess() {
		return createProcessInternal();
	}

	private AbstractLRProcessImpl createProcessInternal() {
		String osName = System.getProperty("os.name");
		if ((!osName.toLowerCase().contains("windows")) &&
			(!osName.toLowerCase().contains("microsoft"))) {
			return new cz.incad.kramerius.processes.os.impl.unix.UnixLRProcessImpl(this, this.pm, this.configuration);
		} else {
			return new WindowsLRProcessImpl(this, this.pm, configuration); 
		}
	}
	
	
	
	@Override
	public LRProcess loadProcess(String uuid, String pid, long start, States state) {
		AbstractLRProcessImpl abs = createProcessInternal();
		abs.setUuid(uuid);
		abs.setDefinition(this);
		abs.setStartTime(start);
		abs.setPid(pid);
		abs.setProcessState(state);
		return abs;
	}

	public String getId() {
		return id;
	}

	@Override
	public String getMainClass() {
		return this.mainClass;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	public LRProcessManager getPm() {
		return pm;
	}

	public void setPm(LRProcessManager pm) {
		this.pm = pm;
	}

	public String getStandardStreamFolder() {
		return standardStreamFolder;
	}

	public void setStandardStreamFolder(String standardStreamFolder) {
		this.standardStreamFolder = standardStreamFolder;
	}

	public String getErrStreamFolder() {
		return errStreamFolder;
	}

	public void setErrStreamFolder(String errStreamFolder) {
		this.errStreamFolder = errStreamFolder;
	}
}
