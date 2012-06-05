package cz.incad.kramerius.processes;

import java.io.File;
import java.util.List;

import cz.incad.kramerius.Constants;

/**
 * Long running process definitions
 * @author pavels
 */
public interface DefinitionManager {

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
	public LRProcessDefinition getLongRunningProcessDefinition(String id);

	/**
	 * Returns all definitions
	 * @return
	 */
	public List<LRProcessDefinition> getLongRunningProcessDefinitions();

	
}
