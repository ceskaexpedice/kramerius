package cz.incad.kramerius.service;

import java.io.IOException;

public interface PolicyService {
    
    /**
     * Set the given policy the tree of Kramerius objects including the root with given PID 
     * @param pid PID of the tree root object
     * @param policyName id of the policy to be set (currently "public" or "private")
     * @throws IOException 
     */
    public void setPolicy(String pid, String policyName) throws IOException;

}
