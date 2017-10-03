package org.gradle

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
//import org.gradle.api.plugins.cargo.CargoBasePlugin

import java.util.concurrent.Callable

class DeployPlugin implements Plugin<ProjectInternal> {
	

	void apply(final ProjectInternal project) {
                // apply java plugin
                //project.getPlugins().apply(CargoBasePlugin.class);
		
                // extensions object and create deployremotecontainer tasks
				//project.extensions.create("deployment", ServersExtension.class, project);		
	}
}
