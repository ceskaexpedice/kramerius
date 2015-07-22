package cz.incad.kramerius.pdf;


/**
 * Interface represents signal for pdf processing algorithm which means that new file should be created
 * @author pavels
 */
@Deprecated
public interface Break {
    
    /**
     * Returns true if new file should be created 
     * @param uuid UUID of page
     * @return true if new file should be created
     */
	public boolean broken(String uuid);
}
