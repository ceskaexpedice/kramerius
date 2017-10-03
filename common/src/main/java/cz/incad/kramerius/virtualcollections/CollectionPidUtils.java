package cz.incad.kramerius.virtualcollections;

public class CollectionPidUtils {
    
    private CollectionPidUtils() {}
    
    public static boolean isCollectionPid(String pid){
        return pid.toLowerCase().startsWith("vc:");
    }
}
