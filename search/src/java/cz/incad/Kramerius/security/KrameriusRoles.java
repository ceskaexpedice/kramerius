package cz.incad.Kramerius.security;

//TODO?: Co s timhle? 
public class KrameriusRoles {
	
	// super admin - muze vse
	public static final String KRAMERIUS_ADMIN = "krameriusAdmin";
	// sprava procesu - je nutne mit 
	public static final String LRPROCESS_ADMIN ="lrProcessAdmin";

	public static final String IMPORT = "import";
	public static final String CONVERT = "convert";
	public static final String REPLICATIONRIGHTS = "replicationrights";
	public static final String ENUMERATOR = "enumerator";
	public static final String REINDEX = "reindex";
	public static final String REPLIKATOR_PERIODICALS = "replikator_periodicals";
	public static final String REPLIKATOR_MONOGRAPHS = "replikator_monographs";
	public static final String DELETE = "delete";
	public static final String EXPORT = "export";
	public static final String SETPRIVATE = "setprivate";
	public static final String SETPUBLIC = "setpublic";
	
}
