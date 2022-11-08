package cz.incad.kramerius.rest.apiNew.client.v60.libs;

import java.util.List;

public interface Instances {
	
	public List<String> allInstances();
	
	public List<String> enabledInstances();
	
	public List<String> disabledInstances();
	
	public void setStatus(String inst, boolean status);
	
	public boolean getStatus(String inst);
	
	public boolean isAnyDisabled();
}
