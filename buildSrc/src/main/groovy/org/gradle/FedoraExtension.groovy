
public class FedoraExtension{
	
		   
	public static final String DOWNLOAD_SITE = "https://github.com/fcrepo4/fcrepo4/releases/download/"	
 
	private String version;
		

	public String getVersion() {
		return this.version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getFile() {
		return "fcrepo-webapp-${this.version}-jetty-console.jar";
	}

	public String getTargetFile() {
		return System.getProperty("user.home") + File.separator + this.getFile();
	}

	public String getLink() {
		return DOWNLOAD_SITE + "fcrepo-${this.version}/"+getFile();
	}
}

