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
package org.kramerius.processes.filetree;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kramerius.processes.utils.TreeModelUtils;

import cz.incad.kramerius.utils.IOUtils;


/**
 * @author pavels
 *
 */
public class FileTreeTest {

    public static final String[] SM_NAMES = "a,b,c,d,e,f,g,i,k,l,m,n,o,p,r,s,t,u,v,x,y,z".split(",");
    public static final String[] H_NAMES = "a,b,c,d,e,f,g,i,k,l,m,n,o,p,r,s,t,u,v,x,y,z".toUpperCase().split(",");
    
    
    @Before
    public void setup() {
        createStructure();
    }


    public static void createStructure() {
        File tmpDir = testDir();
        if (tmpDir.exists()) tmpDir.mkdirs();
        for (int i = 0; i < SM_NAMES.length; i++) {
            File smFolder = new File(tmpDir,SM_NAMES[i]);
            smFolder.mkdirs();
            for (int j = 0; j < H_NAMES.length; j++) {
                File hFolder = new File(smFolder, H_NAMES[j]);
                hFolder.mkdirs();
            }
            
        }
    }


    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(testDir());
    }

    public static File testDir() {
        File f = new File(System.getProperty("java.io.tmpdir"));
        File tmpDir = new File(f,"test");
        return tmpDir;
    }

    @Test
    public void testFiles() {
        TreeItem root = TreeModelUtils.prepareTreeModel(testDir(), null);
        Assert.assertNotNull(root);
        Assert.assertFalse(root.isLeaf());
        Assert.assertTrue(root.getChildren().size() == SM_NAMES.length);
        for (int i = 0; i < root.getChildren().size(); i++) {
            String itmName = root.getChildren().get(i).getItemName();
            Assert.assertTrue(containsName(itmName, SM_NAMES));
        }
    }
    
    
    public boolean containsName(String fname, String[] names) {
        return Arrays.asList(names).contains(fname);
    }
}
