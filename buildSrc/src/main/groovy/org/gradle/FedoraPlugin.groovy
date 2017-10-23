package org.gradle
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.MavenPluginConvention
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
class FedoraPlugin implements Plugin<ProjectInternal> {
	

	
	void apply(ProjectInternal project) {
		println "TAK NEVIM !!!! "
		// apply java plugin
		project.getPlugins().apply(JavaPlugin.class);


		// extensions object
		project.extensions.create("fedora", FedoraExtension.class);

		Download downTask = project.getTasks().create("fedoraDownload", Download.class);
		StartFedoraTask feExec = project.getTasks().create("feduraExecute", StartFedoraTask.class);

		project.getTasks().getByName("test").dependsOn(feExec);
		feExec.dependsOn(downTask);
	}
}
