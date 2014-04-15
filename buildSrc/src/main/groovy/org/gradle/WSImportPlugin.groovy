package org.gradle
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.Delete

import org.gradle.api.file.DirectoryTree
import org.gradle.api.file.FileVisitor
import org.gradle.api.file.FileVisitDetails
import java.lang.reflect.Method

import java.util.concurrent.Callable

import static org.gradle.api.plugins.JavaPlugin.COMPILE_CONFIGURATION_NAME
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
					//Set<File> files = sourceSet.getResources().getFiles();
                                        Set<DirectoryTree> trees =  sourceSet.getResources().getSrcDirTrees();

					// lookup wsdl files
                                        final Map<File, String> relativePaths = new HashMap<File,String>();
					for(DirectoryTree dt:trees) {
                                                dt.visit(new FileVisitor() {
                                                        public void visitDir(FileVisitDetails dirDetails) {
                                                        }
                                                        public void visitFile(FileVisitDetails fileDetails) {
                                                                if (fileDetails.getFile().getName().toLowerCase().endsWith(".wsdl")) {
                                                                        relativePaths.put(fileDetails.getFile(), fileDetails.getRelativePath().toString());
                                                                }
                                                        }

                                                });
                                        }                                        
                                        

					// define tasks
					final String taskName = sourceSet.getTaskName("wsimport", "WSImport");
					
					
					WSImportTask wTask = project.getTasks().create(taskName, WSImportTask.class);

                                        final String deleteTaskName = sourceSet.getTaskName("GenerateDelete", "");
                                        Delete dtask = project.getTasks().create(deleteTaskName, Delete.class);
 
					// lookup wsdl files
                                        /*
					for(File f:files) {
						if (f.getName().toLowerCase().endsWith(".wsdl")) {
                                        		wTask.addWsdl(f);
						}
					}*/
                                        

                                        Set<File> keySet = relativePaths.keySet();
					for(File w:keySet) {
                                                wTask.addWsdl(w,relativePaths.get(w));
                                        }					
					
					
					wTask.getConventionMapping().map("xjcClasspath", new Callable<Object>() {
						public Object call() throws Exception {
							Object obj = project.getConfigurations().getByName(WSImportPlugin.WSIMPORT_CONFIGURATION_NAME).copy()
							.setTransitive(true);
							return obj;
						}
					});
					
					
					// Set up the output directory 
					final String outputDirectoryName = String.format("%s/generated-src/wsimport/%s", project.getProjectDir(), sourceSet.getName());
					final File outputDirectory = new File(outputDirectoryName);
					wTask.setOutputDirectory(outputDirectory);
					sourceSet.getJava().srcDir(outputDirectory);
                                        dtask.delete(outputDirectory);
					
					//uptodate flag
					wTask.getOutputs().file(new File(outputDirectory,"API."+taskName+".generated").getAbsolutePath());
					wTask.getOutputs().upToDateWhen({	
						File f = new File(outputDirectory,"API."+taskName+".generated");
						return f.exists();
					});

					// make sure that task is part of build 
					project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(taskName);
                                        // custom delete
					project.getTasks().getByName("clean").dependsOn(dtask);
					
				}
		});


	}
}
