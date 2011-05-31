package cz.incad.kramerius.rights.server;

public enum SecuredActions {

    rightsadmin("rightsadmin"), rightssubadmin("rightssubadmin");

    private String formalName;

    private SecuredActions(String formalName) {
        this.formalName = formalName;
    }

    public String getFormalName() {
        return formalName;
    }

}
