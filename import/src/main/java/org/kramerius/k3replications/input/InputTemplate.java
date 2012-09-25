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
import org.kramerius.processes.utils.ResourceBundleUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * @author pavels
 *
 */
public class InputTemplate implements ProcessInputTemplate {

    @Inject
    KConfiguration configuration;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    ResourceBundleService resourceBundleService;
    
    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        // root ?
        File homeFolder = new File(KConfiguration.getInstance().getProperty("import.directory")).getParentFile();
        InputStream iStream = this.getClass().getResourceAsStream("replicationtemplate.st");
        
        TreeItem rootNode = new TreeItem(homeFolder.getPath(), homeFolder.getName());
        Stack<TreeItemFileMap> pStack = new Stack<TreeItemFileMap>();
        pStack.push(new TreeItemFileMap(rootNode,homeFolder));
        while(!pStack.isEmpty()) {
            TreeItemFileMap pair = pStack.pop();
            File folder = pair.getFoder();
            File[] lFiles = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (lFiles != null) {
                for (File subFolder : lFiles) {
                    if (subFolder.isDirectory() && subFolder.getName().equals("lp")) continue;
                    if (subFolder.isDirectory() && subFolder.getName().equals("deepZoom")) continue;
                    if (subFolder.isDirectory() && subFolder.getName().equals("export")) continue;
                    TreeItem subItem = new TreeItem(subFolder.getPath(),subFolder.getName());
                    pair.getItem().addItem(subItem);
                    TreeItemFileMap subpair = new TreeItemFileMap(subItem,subFolder);
                    pStack.add(subpair);
                }
            }
        }
        
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("form");

        //form(migrationDirectory,targetDirectory,importRootDirectory, bundle) ::=<<

        template.setAttribute("migrationDirectory", KConfiguration.getInstance().getProperty("import.directory"));
        template.setAttribute("targetDirectory",  KConfiguration.getInstance().getProperty("import.directory"));
        template.setAttribute("importRootDirectory", rootNode);
    
        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());
        template.setAttribute("bundle", ResourceBundleUtils.resourceBundleMap(resbundle));
        
        writer.write(template.toString());
    }
    
    
}
