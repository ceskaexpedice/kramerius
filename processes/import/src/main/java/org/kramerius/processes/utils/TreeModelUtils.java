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
package org.kramerius.processes.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Stack;

import org.kramerius.processes.filetree.TreeItem;
import org.kramerius.processes.filetree.TreeItemFileMap;
import org.kramerius.processes.filetree.TreeModelFilter;

/**
 * HTML tree utils
 * @author pavels
 */
public class TreeModelUtils {

    /**
     * Creates tree model
     * @param homeFolder
     * @return
     */
    public static TreeItem prepareTreeModel(File homeFolder, TreeModelFilter filter) {
        TreeItem rootNode = new TreeItem(homeFolder.getPath(), homeFolder.getName());
        Stack<TreeItemFileMap> pStack = new Stack<TreeItemFileMap>();
        pStack.push(new TreeItemFileMap(rootNode, homeFolder));
        while (!pStack.isEmpty()) {
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
                    if ((filter == null) || filter.accept(subFolder)) {
                        TreeItem subItem = new TreeItem(subFolder.getPath(), subFolder.getName());
                        pair.getItem().addItem(subItem);
                        TreeItemFileMap subpair = new TreeItemFileMap(subItem, subFolder);
                        pStack.add(subpair);
                    }
                }
            }
        }
        return rootNode;
    }
}
