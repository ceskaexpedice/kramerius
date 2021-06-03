package cz.incad.kramerius.security.labels;

public interface Label {

    public static int DEFAULT_PRIORITY = 1;

    public int getId();

    public String getName();

    public String getDescription();

    public String getGroup();

    public int getPriority();

    public Label getUpdatedPriorityLabel(int priprity);


}
