package cz.incad.kramerius.security;

import cz.incad.kramerius.security.labels.Label;

public interface RightCriteriumLabelAware {

    public Label getLabel();

    public void setLabel(Label label);
}
