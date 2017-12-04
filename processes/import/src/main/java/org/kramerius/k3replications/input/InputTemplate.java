/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.kramerius.k3replications.input;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.kramerius.processes.filetree.TreeItem;
import org.kramerius.processes.filetree.TreeItemFileMap;
import org.kramerius.processes.filetree.TreeModelFilter;
import org.kramerius.processes.utils.BasicStringTemplateGroup;
import org.kramerius.processes.utils.OtherSettingsTemplate;
import org.kramerius.processes.utils.TreeModelUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.stemplates.ResourceBundleUtils;

/**
 * @author pavels
 *
 */
public class InputTemplate implements ProcessInputTemplate {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(InputTemplate.class.getName());
    
    @Inject
    KConfiguration configuration;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    ResourceBundleService resourceBundleService;
    
    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        // root ?
        File homeFolder = new File(configuration.getProperty("migration.directory"));
        InputStream iStream = this.getClass().getResourceAsStream("replicationtemplate.st");
        
        TreeItem rootNode = TreeModelUtils.prepareTreeModel(homeFolder,new TreeModelFilter() {
            String[] NAMES = { "lp","exported","deepZoom" };
            @Override
            public boolean accept(File file) {
                String sname = file.getName();
                for (String nm : NAMES) {
                    if (nm.equals(sname)) return false;
                }
                return true;
            }
        });

        
        
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        templateGroup.setSuperGroup(BasicStringTemplateGroup.getBasicProcessesGroup());
        StringTemplate template = templateGroup.getInstanceOf("form");

        template.setAttribute("migrationDirectory", configuration.getProperty("migration.directory"));
        template.setAttribute("targetDirectory",  configuration.getProperty("migration.target.directory"));
        template.setAttribute("importRootDirectory", rootNode);
    
        Boolean visibility = configuration.getConfiguration().getBoolean("convert.defaultRights");
        template.setAttribute("visibility", visibility);
        LOGGER.info("visibility :"+visibility);
        
        Boolean importToFedora = !configuration.getConfiguration().getBoolean("ingest.skip");
        Boolean startIndexer = configuration.getConfiguration().getBoolean("ingest.startIndexer");

        OtherSettingsTemplate oSettings = OtherSettingsTemplate.disectTemplate(importToFedora, startIndexer);
        template.setAttribute("otherSettingsTemplate", oSettings.name());

        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());
        template.setAttribute("bundle", ResourceBundleUtils.resourceBundleMap(resbundle));
        
        writer.write(template.toString());
    }
    
}
