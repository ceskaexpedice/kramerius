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
package org.kramerius.replications;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.kramerius.replications.SecondPhase.DONEController;

import cz.incad.kramerius.utils.pid.LexerException;


/**
 * @author pavels
 *
 */
public class DONEControllerTest {

    static String[] FILES = {"6b7da6a9-c1a7-11df-b7b5-001b63bd97ba.fo.done","7d93a027-c1a7-11df-b7b5-001b63bd97ba.fo.done","76725e81-c1a7-11df-b7b5-001b63bd97ba.fo.done",
    "6b767ab8-c1a7-11df-b7b5-001b63bd97ba.fo.done","708e09ac-c1a7-11df-b7b5-001b63bd97ba.fo.done","77964512-c1a7-11df-b7b5-001b63bd97ba.fo.done",
    "6e1b0dda-c1a7-11df-b7b5-001b63bd97ba.fo.done","71bee88d-c1a7-11df-b7b5-001b63bd97ba.fo.done","78b91a33-c1a7-11df-b7b5-001b63bd97ba.fo.done",
    "6f4f6f2b-c1a7-11df-b7b5-001b63bd97ba.fo.done","72e7d82e-c1a7-11df-b7b5-001b63bd97ba.fo.done","79dcd9b4-c1a7-11df-b7b5-001b63bd97ba.fo.done",
    "7b124c75-c1a7-11df-b7b5-001b63bd97ba.fo.done","7423656f-c1a7-11df-b7b5-001b63bd97ba.fo.done",
    "7c6a1446-c1a7-11df-b7b5-001b63bd97ba.fo.done","7546fde0-c1a7-11df-b7b5-001b63bd97ba.fo.done"};
    
    @Test
    public void testDONEController() throws LexerException {
        File rootFolder = EasyMock.createMock(File.class);
        EasyMock.expect(rootFolder.exists()).andReturn(true).anyTimes();
        EasyMock.expect(rootFolder.getName()).andReturn("abs").anyTimes();
        EasyMock.expect(rootFolder.getAbsolutePath()).andReturn("/abs").anyTimes();
        
        File zeroFolder = EasyMock.createMock(File.class);
        EasyMock.expect(zeroFolder.getName()).andReturn("0").anyTimes();
        EasyMock.expect(zeroFolder.isDirectory()).andReturn(true).anyTimes();
        EasyMock.expect(zeroFolder.isFile()).andReturn(false).anyTimes();
        EasyMock.expect(zeroFolder.getAbsolutePath()).andReturn("/abs/0").anyTimes();

        EasyMock.expect(rootFolder.listFiles()).andReturn(new File[] {zeroFolder}).anyTimes();

        
        List<File> mockFiles = new ArrayList<File>();
        for (String f : FILES) {
            mockFiles.add(mockFile(zeroFolder, f));
        }
        EasyMock.expect(zeroFolder.listFiles()).andReturn(mockFiles.toArray(new File[FILES.length])).anyTimes();
        EasyMock.replay(rootFolder,zeroFolder);
        for (File file : mockFiles) {
            EasyMock.replay(file);
        }
        
        DONEController dcontrol = new DONEController(rootFolder, 10);
        File found = dcontrol.findPid("uuid:6b767ab8-c1a7-11df-b7b5-001b63bd97ba");
        Assert.assertNotNull(found);
        found = dcontrol.findPid("uuid:6b767ab8-c1a7-11df-b7b5-001b63bd97bc");
        Assert.assertNull(found);
        
    }

    /**
     * @param zeroFolder
     * @param f
     * @return
     */
    private File mockFile(File zeroFolder, String f) {
        File file = EasyMock.createMock(File.class);
        EasyMock.expect(file.getName()).andReturn(f).anyTimes();
        EasyMock.expect(file.getAbsolutePath()).andReturn("/abs/0/"+f).anyTimes();
        EasyMock.expect(file.listFiles()).andReturn(null).anyTimes();
        return file;
    }
}
