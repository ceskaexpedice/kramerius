package cz.incad.kramerius.security.labels;

import java.util.regex.Pattern;

public interface Label {

    public static final Pattern ACCEPTABLE_LABEL_NAME_REGEXP= Pattern.compile("[a-zA-Z][a-zA-Z_0-9-/:]+");

    public static int DEFAULT_PRIORITY = 1;

    public int getId();

    public String getName();

    public String getDescription();

    public String getGroup();

    public int getPriority();

    public Label getUpdatedPriorityLabel(int priprity);


}
