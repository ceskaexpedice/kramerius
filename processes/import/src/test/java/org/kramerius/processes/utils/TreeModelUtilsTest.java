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
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kramerius.processes.filetree.FileTreeTest;
import org.kramerius.processes.filetree.TreeItem;
import org.kramerius.processes.filetree.TreeModelFilter;

/**
 * @author pavels
 *
 */
public class TreeModelUtilsTest {

    
    @Before
    public void setup() {
        FileTreeTest.createStructure();
    }
    
    
    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(FileTreeTest.testDir());
    }
    
    @Test
    public void testModel() {
        TreeItem model = TreeModelUtils.prepareTreeModel(FileTreeTest.testDir(), null);
        Assert.assertNotNull(model);
        Assert.assertTrue(model.getChildren().size() == FileTreeTest.H_NAMES.length);
        for (TreeItem item : model.getChildren()) {
            List<TreeItem> children = item.getChildren();
            Assert.assertTrue(children.size() == FileTreeTest.SM_NAMES.length);
        }
    }

    @Test
    public void testModelFilter() {
        TreeItem model = TreeModelUtils.prepareTreeModel(FileTreeTest.testDir(), new TreeModelFilter() {
            
            @Override
            public boolean accept(File file) {
                if (file.getName().toLowerCase().equals("a")) return false;
                else return true;
            }
        });
        Assert.assertNotNull(model);
        Assert.assertTrue(model.getChildren().size() == FileTreeTest.H_NAMES.length-1);
        for (TreeItem item : model.getChildren()) {
            List<TreeItem> children = item.getChildren();
            Assert.assertTrue(children.size() == FileTreeTest.SM_NAMES.length-1);
        }
    }
}
