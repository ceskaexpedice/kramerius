package org.gradle

import org.gradle.api.file.*;
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.artifacts.*;

import org.gradle.api.file.FileCollection;
/*
import org.gradle.api.plugins.antlr.internal.GenerationPlan;
import org.gradle.api.plugins.antlr.internal.GenerationPlanBuilder;
import org.gradle.api.plugins.antlr.internal.MetadataExtracter;
import org.gradle.api.plugins.antlr.internal.XRef;
*/

import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import com.sun.tools.xjc.*

/** XJC Task; call as java code */
public class XJCTask extends DefaultTask {
	
	/** Default package name */
	public static final String DEFAULT_PACKAGE_NAME="com.qbizm.kramerius.imp.jaxb";

	private static final Logger LOGGER = LoggerFactory.getLogger(XJCTask.class);
	
    
	private FileCollection xjcClasspath;
	private File outputDirectory;
	private String packageName = DEFAULT_PACKAGE_NAME;
	private List<File> xsds = new ArrayList<File>();
	private String catalog;
		
	
	public String getPackageName() {
		return this.packageName;
	}
	
	public void setPackageName(String pckg) {
		this.packageName = pckg;
	}
    
	
	public void setXjcClasspath(FileCollection p) {
		println "setting classpath $p"
		this.xjcClasspath = p;
    	}
    
	@OutputDirectory
	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

    	
	@InputFiles
	public FileCollection getXjcClasspath() {
		return this.xjcClasspath;
	}
    	
	public void addXsd(File xsd) {
		this.xsds.add(xsd);
	}
	
	public void removeXsd(File xsd) {
		this.xsds.remove(xsd);
	}
	
	public List<File> getXsds() {
		return new ArrayList(this.xsds);
	}

	public void setCatalog(String cat) {
		this.catalog = cat;
	} 	

	public String getCatalog() {
		return this.catalog;
	}
	private void configureExt(JAXBExtensions ext) {
		setPackageName(ext.getPackageName());
		setCatalog(ext.getCatalog());
	}
    
	@TaskAction
	public void generate() {

	System.setProperty('javax.xml.accessExternalSchema', 'all')
	JAXBExtensions ext = getProject().getExtensions().getByType(JAXBExtensions.class);
    	    if (ext != null) {
    	    	    configureExt(ext);
    	    }

    	    ant.taskdef(name: 'xjc', classname: 'com.sun.tools.xjc.XJC2Task', classpath: getXjcClasspath().asPath)
    	    for(File f:this.xsds) {
		    if (catalog != null) {
	    	    	    ant.xjc(schema: f.getAbsolutePath(), package: this.packageName, destdir: outputDirectory.getAbsolutePath(),removeOldOutput: true, catalog: this.catalog) {
	arg(value:"-disableXmlSecurity")
	}
		    } else {
	    	    	    ant.xjc(schema: f.getAbsolutePath(), package: this.packageName, destdir: outputDirectory.getAbsolutePath(),removeOldOutput: true) {
	arg(value:"-disableXmlSecurity")
}
		    }		
    	    }
	    
	    new File(this.outputDirectory,"JAXB."+getName()+".generated").createNewFile();
    }
}
