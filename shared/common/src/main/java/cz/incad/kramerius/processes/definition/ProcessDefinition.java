package cz.incad.kramerius.processes.definition;

import java.util.List;

/**
 * This is process definition
 * @author pavels
 */
public interface ProcessDefinition {
	
	/**
	 * Returns description of process
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Main class. At the moment only java processes are supported !
	 * @return
	 */
	public String getMainClass();

	/**
	 * Java process parameters (-Xmx, -Xms, etc..)
	 * @return
	 */
	public List<String> getJavaProcessParameters();
	
	/**
	 * Program parameters (main method parameters)
	 * @return
	 */
    public List<String> getParameters();
	
	
	/**
	 * Libraries for the processs
	 * @return
	 */
	public String getLibsDir();

	/**
	 * Definition identification
	 * @return
	 */
	public String getId();
	
	/**
	 * Returns file error stream file 
	 * @return
	 */
	public String getErrStreamFolder();
	
	/**
	 * Returns standard stream file
	 * @return
	 */
	public String getStandardStreamFolder();


	@Deprecated
	public ProcessDefinitionAction getLogsAction();

	
	/**
	 * Actions defined over LR process
	 * @return
	 */
	@Deprecated
	public List<ProcessDefinitionAction> getActions();

	
	/**
	 * Returns input template class
	 * @return
	 */
	public String getInputTemplateClass();

	/**
	 * REturns true if input template defined
	 * @return
	 */
	public boolean isInputTemplateDefined();

	/**
	 * Returns output template class
	 * @return
	 */
	public List<String> getOutputTemplateClasses();

	
	/**
	 * Returns true if output template is defined
	 * @return
	 */
	public boolean isOutputTemplatesDefined();
	

	/**
	 * Secured action
	 * @return
	 */
	public String getSecuredAction();

	/**
	 * Returns true, if the system should observe stderr and throw WarningException (if found any messages)
	 * @see WarningException
	 * @return
	 */
	public boolean isCheckedErrorStream();
}
