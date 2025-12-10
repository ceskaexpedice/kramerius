package cz.incad.kramerius.processes.definition;

import java.util.List;

/**
 * This is process definition
 * @author pavels
 */
public interface ProcessDefinition {

    /**
     * Definition identification
     * @return
     */
    public String getId();

    /**
	 * Returns description of process
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Java process parameters (-Xmx, -Xms, etc..)
	 * @return
	 */
	public List<String> getJavaProcessParameters();

	/**
	 * Secured action
	 * @return
	 */
	public String getSecuredAction();

}
