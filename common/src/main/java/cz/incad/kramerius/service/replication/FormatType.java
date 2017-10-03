package cz.incad.kramerius.service.replication;

/**
 * Replication formats
 * @author pavels
 */
public enum FormatType {
	
	/** No changes in FOXML format */
	IDENTITY(IdentityFormat.class), 
	/** CDK format */
	CDK(CDKFormat.class), 
	
	/** K4 replication format */
	EXTERNALREFERENCES(ExternalReferencesFormat.class),
	
	/** K4 replication format */
	EXTERNALREFERENCESANDREMOVECOLS(ExternalReferencesAndRemoveCollectionsFormat.class);


	private Class<?> clazz;

	private FormatType(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
}
