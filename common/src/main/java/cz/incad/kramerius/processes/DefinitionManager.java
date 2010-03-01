package cz.incad.kramerius.processes;

import java.util.List;

/**
 * Long running process definitions
 * @author pavels
 */
public interface DefinitionManager {

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
