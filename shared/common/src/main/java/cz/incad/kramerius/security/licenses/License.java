package cz.incad.kramerius.security.licenses;

import java.util.regex.Pattern;

/**
 * Represents a license object
 * @author happy
 */
public interface License {

    /**
     * License name must match following pattern 
     */
    public static final Pattern ACCEPTABLE_LABEL_NAME_REGEXP= Pattern.compile("[a-zA-Z][a-zA-Z_0-9-/:]+");

    /**
     * Default license priority
     */
    public static int DEFAULT_PRIORITY = 1;
    
    /**
     * Returns a unique license identifier
     * @return
     */
    public int getId();

    /**
     * Returns the name of the license
     * @return
     */
    public String getName();

    /**
     * Basic the description of the license
     * @return
     */
    public String getDescription();

    /**
     * Returns the group of license
     * @see LicensesManager#GLOBAL_GROUP_NAME
     * @see LicensesManager#LOCAL_GROUP_NAME
     * @return
     */
    public String getGroup();

    /**
     * Returns the priority of license
     * @return
     */
    public int getPriority();

    /**
     * Updating priority of license 
     * @param priprity
     * @return
     */
    public License getUpdatedPriorityLabel(int priprity);
    
}
