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
package cz.incad.kramerius.processes.mock.wizard;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FolderTemplates implements ProcessInputTemplate {

    @Inject
    KConfiguration configuration;
    
    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        File homeFolder = new File(System.getProperty("user.home")+File.separator+".kramerius4");
        InputStream iStream = this.getClass().getResourceAsStream("wizard.st");
        
        TreeItem rootNode = new TreeItem(homeFolder.getPath(), homeFolder.getName());
        Stack<Pair> pStack = new Stack<Pair>();
        pStack.push(new Pair(rootNode,homeFolder));
        while(!pStack.isEmpty()) {
            Pair pair = pStack.pop();
            File folder = pair.getFoder();
            File[] lFiles = folder.listFiles(new FileFilter() {
                
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (lFiles != null) {
                for (File subFolder : lFiles) {
                    TreeItem subItem = new TreeItem(subFolder.getPath(),subFolder.getName());
                    pair.getItem().addItem(subItem);
                    Pair subpair = new Pair(subItem,subFolder);
                    pStack.add(subpair);
                }
            }
        }
        
        StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = templateGroup.getInstanceOf("form");
        template.setAttribute("rootNode", rootNode);
        
        writer.write(template.toString());
    }
    
    static class Pair {

        private TreeItem item;
        private File foder;
        
        public Pair(TreeItem item, File foder) {
            super();
            this.item = item;
            this.foder = foder;
        }

        public TreeItem getItem() {
            return item;
        }
        
        public File getFoder() {
            return foder;
        };
        
    }
    
    
    public static class TreeItem {
        
        private List<TreeItem> children = new ArrayList<FolderTemplates.TreeItem>();
        private String itemName;
        private String id;
        

        public TreeItem(String id, String itemName) {
            super();
            this.itemName = itemName;
            this.id = id;
        }

        public void addItem(TreeItem item) {
            children.add(item);
        }
        
        public void removeItem(TreeItem item) {
            children.remove(item);
        }
        
        public List<TreeItem> getChildren() {
            return children;
        }

        public String getItemName() {
            return itemName;
        }

        public String getId() {
            return id;
        }
        
        public boolean isLeaf() {
            return this.children.isEmpty(); 
        }

    }
}
