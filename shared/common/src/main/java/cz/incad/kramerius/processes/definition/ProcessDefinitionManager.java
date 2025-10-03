package cz.incad.kramerius.processes.definition;

import java.io.File;
import java.util.List;

import cz.incad.kramerius.Constants;

/**
 * Process definitions
 * @author pavels
 */
public interface ProcessDefinitionManager {

	public static final String CONFIGURATION_FILE = Constants.WORKING_DIR+File.separator+"lp.xml";
	public static final String DEFAULT_LP_WORKDIR = Constants.WORKING_DIR+File.separator+"lp";

	/**
	 * Load definitions from configuration
	 */
	public void load();

	/**
	 * Returns definition associated with given id
	 * @param id ID of lr process definition
	 * @return
	 */
	public ProcessDefinition getProcessDefinition(String id);

	/**
	 * Returns all definitions
	 * @return
	 */
	public List<ProcessDefinition> getProcessDefinitions();

	
}
