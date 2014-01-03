package org.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GreetingTask extends DefaultTask {
    
	String greeting = 'hello from GreetingTask'

	@TaskAction
	def greet() {
		ant.taskdef(name: 'xjc', classname: 'com.sun.tools.xjc.XJC2Task', classpath: configurations.generate.asPath)

		println greeting
	}
}