package cz.incad.kramerius.security.licenses;

import java.util.regex.Pattern;

public interface License {

    public static final Pattern ACCEPTABLE_LABEL_NAME_REGEXP= Pattern.compile("[a-zA-Z][a-zA-Z_0-9-/:]+");

    public static int DEFAULT_PRIORITY = 1;

    public int getId();

    public String getName();

    public String getDescription();

    public String getGroup();

    public int getPriority();

    public License getUpdatedPriorityLabel(int priprity);


}
