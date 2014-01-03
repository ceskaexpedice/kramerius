package org.gradle

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.antlr.internal.AntlrSourceVirtualDirectoryImpl;
import org.gradle.api.tasks.SourceSet;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.Callable;

import static org.gradle.api.plugins.JavaPlugin.COMPILE_CONFIGURATION_NAME;


/**
 * Call wsimport ant task. Must be defined as taskdef('wsimport',... and ant.wsimport...
 */
class WSImportPlugin implements Plugin<ProjectInternal> {
	
	public static final String WSIMPORT_CONFIGURATION_NAME = "wsimport";

	
	void apply(ProjectInternal project) {
		// apply java plugin
		project.getPlugins().apply(JavaPlugin.class);

		Configuration wsimpConf = project.getConfigurations().create(WSIMPORT_CONFIGURATION_NAME).setVisible(false)
                .setTransitive(false).setDescription("The wsimport libs");
                project.getConfigurations().getByName(COMPILE_CONFIGURATION_NAME).extendsFrom(wsimpConf);
		
		// extensions object
		project.extensions.create("wsimportconf", WSImportExtensions.class);		

		
		project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(
			new Action<SourceSet>() {
				public void execute(SourceSet sourceSet) {
					// source files
					Set<File> files = sourceSet.getResources().getFiles();
					// define tasks
					final String taskName = sourceSet.getTaskName("wsimport", "WSImport");
					
					
					WSImportTask wTask = project.getTasks().create(taskName, WSImportTask.class);

					// lookup wsdl files
					for(File f:files) {
						if (f.getName().toLowerCase().endsWith(".wsdl")) {
							wTask.addWsdl(f);
						}
					}
					
					
					
					wTask.getConventionMapping().map("xjcClasspath", new Callable<Object>() {
						public Object call() throws Exception {
							Object obj = project.getConfigurations().getByName(WSImportPlugin.WSIMPORT_CONFIGURATION_NAME).copy()
							.setTransitive(true);
							return obj;
						}
					});
					
					
					// Set up the output directory 
					final String outputDirectoryName = String.format("%s/generated-src/wsimport/%s",project.getBuildDir(), sourceSet.getName());
					final File outputDirectory = new File(outputDirectoryName);
					wTask.setOutputDirectory(outputDirectory);
					sourceSet.getJava().srcDir(outputDirectory);
					
					//uptodate flag
					wTask.getOutputs().file(new File(outputDirectory,"API."+taskName+".generated").getAbsolutePath());
					wTask.getOutputs().upToDateWhen({	
						File f = new File(outputDirectory,"API."+taskName+".generated");
						return f.exists();
					});

					// make sure that task is part of build 
					project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(taskName);
					
				}
		});


	}
}
