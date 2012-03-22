/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.processes.imported;

import java.io.File;
import java.util.List;

import cz.incad.Kramerius.processes.imported.views.FailedImportViewObject;
import cz.incad.Kramerius.processes.imported.views.ImportsViewObject;
import cz.incad.Kramerius.processes.imported.views.SuccessfulImportViewObject;
import junit.framework.Assert;
import junit.framework.TestCase;

public class ImportsViewObjectTest extends TestCase {

    public void testViewObject() {
        String userdir = System.getProperty("user.dir");
        String folderName = this.getClass().getPackage().getName().replace('.', File.separatorChar);
        ImportsViewObject iv = new ImportsViewObject(new File(userdir+File.separator+"src"+File.separator+"test"+File.separator+folderName+File.separator+"res"));

        List<FailedImportViewObject> fails = iv.getFails();
        Assert.assertTrue(fails.size() == 1);
        FailedImportViewObject failedImport = fails.get(0);

        String failName = failedImport.getName();
        Assert.assertTrue(failName.equals("1801-2620:1912"));
        
        String failException = failedImport.getException();
        Assert.assertTrue(failException.equals("java.lang.RuntimeException: com.sun.xml.internal.ws.client.ClientTransportException: The server sent HTTP status code 302: Moved Temporarily"));
        
        
        List<SuccessfulImportViewObject> successed = iv.getItems();
        Assert.assertTrue(successed.size() == 1);
        
        SuccessfulImportViewObject succ = successed.get(0);
        String data = succ.getData();
        Assert.assertEquals("1803-5809 1885-1887",data);
        
        String pid = succ.getPid();
        Assert.assertEquals(pid, "uuid:96f9c48f-e640-11de-a504-001143e3f55c");

    }
    
}
