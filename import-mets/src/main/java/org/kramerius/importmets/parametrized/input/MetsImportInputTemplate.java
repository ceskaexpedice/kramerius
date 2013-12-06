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
package org.kramerius.importmets.parametrized.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.kramerius.processes.filetree.TreeItem;
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
public class MetsImportInputTemplate implements ProcessInputTemplate {

    @Inject
    KConfiguration configuration;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    ResourceBundleService resourceBundleService;

    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        File homeFolder = new File(configuration.getProperty("convert.directory"));
        InputStream iStream = this.getClass().getResourceAsStream("metsimport.stg");
        
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
        StringTemplateGroup parametrizedconvert = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        parametrizedconvert.setSuperGroup(BasicStringTemplateGroup.getBasicProcessesGroup());
        
        StringTemplate template = parametrizedconvert.getInstanceOf("form");

        template.setAttribute("targetDirectory", configuration.getProperty("convert.target.directory"));
        template.setAttribute("convertDirectory", configuration.getProperty("convert.directory"));
        template.setAttribute("convertRootDirectory",  rootNode);

        Boolean val = configuration.getConfiguration().getBoolean("convert.defaultRights");
        template.setAttribute("visibility", val);

        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());
        template.setAttribute("bundle", ResourceBundleUtils.resourceBundleMap(resbundle));

        Boolean importToFedora = !configuration.getConfiguration().getBoolean("ingest.skip");
        Boolean startIndexer = configuration.getConfiguration().getBoolean("ingest.startIndexer");

        OtherSettingsTemplate oSettings = OtherSettingsTemplate.disectTemplate(importToFedora, startIndexer);
        template.setAttribute("otherSettingsTemplate", oSettings.name());

        writer.write(template.toString());
    }

}
