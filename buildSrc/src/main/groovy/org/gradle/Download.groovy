import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import FedoraExtension;

class Download extends DefaultTask {

    public static final String DOWNLOAD_HOME=System.getProperty("user.home");

    String sourceUrl
    String target

	
	private void configureExt(FedoraExtension ext) {
		this.sourceUrl = ext.getLink();
		this.target = DOWNLOAD_HOME;
	}


	
    @TaskAction
    void download() {
		//LOGGER.log(Level.FINE, "executing  download");
		FedoraExtension ext = getProject().getExtensions().getByType(FedoraExtension.class);
		if (ext != null) {
            configureExt(ext);
		}
        if (!new File(ext.getTargetFile()).exists()) {
            ant.get(src: sourceUrl, dest: target)
        } else {
            this.logger.info("Fedora already downloaded")
        }
    }
}
