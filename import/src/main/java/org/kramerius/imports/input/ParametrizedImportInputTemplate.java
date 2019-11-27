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
package org.kramerius.imports.input;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.stemplates.ResourceBundleUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.kramerius.processes.filetree.TreeItem;
import org.kramerius.processes.filetree.TreeModelFilter;
import org.kramerius.processes.utils.TreeModelUtils;

import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class ParametrizedImportInputTemplate implements ProcessInputTemplate {

    @Inject
    KConfiguration configuration;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    ResourceBundleService resourceBundleService;
    
    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        // root ?
        File homeFolder = new File(KConfiguration.getInstance().getProperty("import.directory"));
        InputStream iStream = this.getClass().getResourceAsStream("parametrizedimport.stg");
        

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

        Random randomGenerator = new Random();
        int idPostfix = randomGenerator.nextInt(2000);
        
        StringTemplateGroup parametrizedimport = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = parametrizedimport.getInstanceOf("form");

        template.setAttribute("importDirectory", KConfiguration.getInstance().getProperty("import.directory"));
        template.setAttribute("importRootDirectory",  rootNode);
    
        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());
        template.setAttribute("bundle", ResourceBundleUtils.resourceBundleMap(resbundle));
        
        Boolean startIndexer = configuration.getConfiguration().getBoolean("ingest.startIndexer");
        template.setAttribute("startIndexer",startIndexer);

        Boolean updateExisting = configuration.getConfiguration().getBoolean("ingest.updateExisting");
        template.setAttribute("updateExisting",updateExisting);

        template.setAttribute("postfixdiv",""+idPostfix);
        
        writer.write(template.toString());
    }
    
}
