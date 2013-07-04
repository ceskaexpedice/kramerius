package cz.incad.kramerius.processes;

import java.util.List;

import cz.incad.kramerius.processes.template.ProcessInputTemplate;

/**
 * This is process definition
 * @author pavels
 */
public interface LRProcessDefinition {
	
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
	 * Factory method that create new process
	 * @param authToken TODO
	 * @param grpToken TODO
	 * @return
	 */
	public LRProcess createNewProcess(String authToken, String grpToken);
	
	/**
	 * Factory method that crate some old process from given information
	 * @param uuid UUID of process UUID of process 
	 * @param pid PID of proccess PID of process
	 * @param start Timestamp when process has been started
	 * @param state Process state
	 * @param bstate Batch process state
	 * @return
	 */
	public LRProcess loadProcess(String uuid, String pid, long start, States state, BatchStates bstate, String name);
	
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
	public LRDefinitionAction getLogsAction();

	
	/**
	 * Actions defined over LR process
	 * @return
	 */
	@Deprecated
	public List<LRDefinitionAction> getActions();

	
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
