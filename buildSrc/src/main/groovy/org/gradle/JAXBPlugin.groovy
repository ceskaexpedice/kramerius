package org.gradle
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

import java.util.concurrent.Callable

import static org.gradle.api.plugins.JavaPlugin.COMPILE_CONFIGURATION_NAME

class JAXBPlugin implements Plugin<ProjectInternal> {
	
	public static final String XJC_CONFIGURATION_NAME = "xjc";
	
	

	void apply(final ProjectInternal project) {
		// apply java plugin
		project.getPlugins().apply(JavaPlugin.class);
		
		// extensions object
		project.extensions.create("jaxbconf", JAXBExtensions.class);		
		
		Configuration jaxbConfiguration = project.getConfigurations().create(XJC_CONFIGURATION_NAME).setVisible(false)
		.setTransitive(true).setDescription("Xjc deps");
		project.getConfigurations().getByName(COMPILE_CONFIGURATION_NAME).extendsFrom(jaxbConfiguration);
		
		project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(
			new Action<SourceSet>() {
				public void execute(SourceSet sourceSet) {
					// source files
					Set<File> files = sourceSet.getResources().getFiles();
					// define tasks
					final String taskName = sourceSet.getTaskName("jaxb", "Jaxbschemas");
					XJCTask xjcTask = project.getTasks().create(taskName, XJCTask.class);

					// lookup xsd files
					for(File f:files) {
						if (f.getName().toLowerCase().endsWith(".xsd")) {
							xjcTask.addXsd(f);
						}
					}
					
					// copy configuration deps
					xjcTask.getConventionMapping().map("xjcClasspath", new Callable<Object>() {
						public Object call() throws Exception {
							return project.getConfigurations().getByName(JAXBPlugin.XJC_CONFIGURATION_NAME).copy()
							.setTransitive(true);
						}
					});
					
					// Set up the output directory 
					final String outputDirectoryName = String.format("%s/generated-src/jaxb/%s",project.getProjectDir(), sourceSet.getName());

					final File outputDirectory = new File(outputDirectoryName);
					xjcTask.setOutputDirectory(outputDirectory);
					sourceSet.getJava().srcDir(outputDirectory);
					
					//uptodate flag
					xjcTask.getOutputs().file(new File(outputDirectory,"JAXB."+taskName+".generated").getAbsolutePath());
					xjcTask.getOutputs().upToDateWhen({	
						return new File(outputDirectory,"JAXB."+taskName+".generated").exists();
					});

					
					// make sure that task is part of build 
					project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(taskName);

                                        // delete generated sources
					final String toDelete = String.format("%s/generated-src",project.getProjectDir());
                                        project.getTasks().getByName('clean').delete(toDelete);                                       
					
				}
		});

	}
}
