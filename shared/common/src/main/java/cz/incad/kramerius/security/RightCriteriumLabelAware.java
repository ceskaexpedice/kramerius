package cz.incad.kramerius.security;

import cz.incad.kramerius.security.licenses.License;

public interface RightCriteriumLabelAware {

    public License getLicense();

    public void setLicense(License license);
}
