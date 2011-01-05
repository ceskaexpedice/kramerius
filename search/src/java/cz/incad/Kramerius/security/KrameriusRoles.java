package cz.incad.Kramerius.security;

//TODO?: Co s timhle? 
// TODO: Smazat
public enum KrameriusRoles {


	// super admin - muze vse
	KRAMERIUS_ADMIN("krameriusAdmin"),
	// sprava procesu - je nutne mit 
	LRPROCESS_ADMIN("lrProcessAdmin"),
	IMPORT( "import"),
	CONVERT( "convert"),
	REPLICATIONRIGHTS( "replicationrights"),
	ENUMERATOR( "enumerator"),
	REINDEX( "reindex"),
	REPLIKATOR_PERIODICALS( "replikator_periodicals"),
	REPLIKATOR_MONOGRAPHS( "replikator_monographs"),
	DELETE( "delete"),
	EXPORT( "export"),
	SETPRIVATE( "setprivate"),
	SETPUBLIC( "setpublic");
	
    private String roleName;

    private KrameriusRoles(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
