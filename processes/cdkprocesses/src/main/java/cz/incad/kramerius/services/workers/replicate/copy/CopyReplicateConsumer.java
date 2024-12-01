package cz.incad.kramerius.services.workers.replicate.copy;

import org.w3c.dom.Element;

public interface CopyReplicateConsumer {
    
    public enum ModifyFieldResult {
        edit,delete,none,
        
        calculated;
    }
    
    public ModifyFieldResult modifyField(Element field);
    
    public void changeDocument(String rootPid, String pid, Element doc);
}
