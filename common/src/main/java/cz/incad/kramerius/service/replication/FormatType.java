package cz.incad.kramerius.service.replication;

public enum FormatType {

	IDENTITY(IdentityFormat.class), CDK(CDKFormat.class), EXTERNALREFERENCES(ExternalReferencesFormat.class);

	private Class<?> clazz;

	private FormatType(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
}
