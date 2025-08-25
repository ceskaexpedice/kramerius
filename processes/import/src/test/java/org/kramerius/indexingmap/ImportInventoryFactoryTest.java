/*
 * Copyright (C) 2025  Inovatika
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
package org.kramerius.indexingmap;

import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.fedoramodel.DatastreamType;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportInventoryFactoryTest {

    @Test
    public void testPeridocalVolumePlan() {
        try (TestDirectoryHandler handler = new TestDirectoryHandler( "data.zip", this.getClass())) {
            Path testDir = handler.getTempDirectory();

            Unmarshaller unmarshaller;
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
                unmarshaller = jaxbContext.createUnmarshaller();
                JAXBContext jaxbdatastreamContext = JAXBContext.newInstance(DatastreamType.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ImportInventoryFactory factory = EasyMock.createMockBuilder(ImportInventoryFactory.class)
                    .withConstructor(Unmarshaller.class, Object.class, ProcessingIndex.class)
                    .withArgs(unmarshaller, new Object(), null)
                    .addMockedMethod("existsInProcessingIndex")
                    .createMock();


            final Set<String> pidsToReturnTrue = new HashSet<>(Arrays.asList(
                    "uuid:045b1250-7e47-11e0-add1-000d606f5dc6"
            ));

            EasyMock.expect(factory.existsInProcessingIndex(EasyMock.anyString()))
                    .andAnswer(new IAnswer<Boolean>() {
                        @Override
                        public Boolean answer() throws Throwable {
                            String pid = (String) EasyMock.getCurrentArguments()[0];
                            return pidsToReturnTrue.contains(pid);
                        }
                    })
                    .anyTimes();
            EasyMock.replay(factory);

            ImportInventory indexMap = factory.createIndexMap(testDir.toFile());
            List<ImportInventoryItem> roots = ScheduleStrategy.indexRoots.scheduleItems(indexMap);
            Assert.assertTrue(roots.size() == 1);
            ImportInventoryItem root = roots.get(0);
            Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6", root.getPid());
            Assert.assertEquals(ImportInventoryItem.TypeOfSchedule.TREE, root.getIndexationPlanType());
            Assert.assertEquals("periodical", root.getModel());
            Assert.assertEquals(true, root.isPresentInProcessingIndex());


            List<ImportInventoryItem> notIndexed = ScheduleStrategy.indexNewImported.scheduleItems(indexMap);
            Assert.assertTrue(notIndexed.size() == 2);

            ImportInventoryItem notIndexedItem = notIndexed.get(0);
            Assert.assertEquals("uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6", notIndexedItem.getPid());
            Assert.assertEquals(ImportInventoryItem.TypeOfSchedule.TREE, notIndexedItem.getIndexationPlanType());
            Assert.assertEquals("periodicalvolume", notIndexedItem.getModel());
            Assert.assertEquals(false, notIndexedItem.isPresentInProcessingIndex());

            ImportInventoryItem indexedItem = notIndexed.get(1);
            Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6", indexedItem.getPid());
            Assert.assertEquals("periodical", indexedItem.getModel());
            Assert.assertEquals(ImportInventoryItem.TypeOfSchedule.OBJECT, indexedItem.getIndexationPlanType());
            Assert.assertEquals(true, indexedItem.isPresentInProcessingIndex());

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPeridocalIssuePlan() {
        try (TestDirectoryHandler handler = new TestDirectoryHandler( "data.zip", this.getClass())) {
            Path testDir = handler.getTempDirectory();

            Unmarshaller unmarshaller;
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
                unmarshaller = jaxbContext.createUnmarshaller();
                JAXBContext jaxbdatastreamContext = JAXBContext.newInstance(DatastreamType.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ImportInventoryFactory factory = EasyMock.createMockBuilder(ImportInventoryFactory.class)
                    .withConstructor(Unmarshaller.class, Object.class, ProcessingIndex.class)
                    .withArgs(unmarshaller, new Object(), null)
                    .addMockedMethod("existsInProcessingIndex")
                    .createMock();


            final Set<String> pidsToReturnTrue = new HashSet<>(Arrays.asList(
                    "uuid:045b1250-7e47-11e0-add1-000d606f5dc6",
                    "uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6"
            ));

            EasyMock.expect(factory.existsInProcessingIndex(EasyMock.anyString()))
                    .andAnswer(new IAnswer<Boolean>() {
                        @Override
                        public Boolean answer() throws Throwable {
                            String pid = (String) EasyMock.getCurrentArguments()[0];
                            return pidsToReturnTrue.contains(pid);
                        }
                    })
                    .anyTimes();
            EasyMock.replay(factory);

            ImportInventory indexMap = factory.createIndexMap(testDir.toFile());
            List<ImportInventoryItem> roots =  ScheduleStrategy.indexRoots.scheduleItems(indexMap); //indexMap.planScheduleIndexForRoots();

            Assert.assertTrue(roots.size() == 1);
            ImportInventoryItem root = roots.get(0);
            Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6", root.getPid());
            Assert.assertEquals(ImportInventoryItem.TypeOfSchedule.TREE, root.getIndexationPlanType());
            Assert.assertEquals("periodical", root.getModel());
            Assert.assertEquals(true, root.isPresentInProcessingIndex());


            List<ImportInventoryItem> notIndexed = ScheduleStrategy.indexNewImported.scheduleItems(indexMap);

            Assert.assertTrue(notIndexed.size() == 3);
            ImportInventoryItem notIndexedItem = notIndexed.get(0);
            Assert.assertEquals("uuid:91214030-80bb-11e0-b482-000d606f5dc6", notIndexedItem.getPid());
            Assert.assertEquals(false, notIndexedItem.isPresentInProcessingIndex());
            Assert.assertEquals(ImportInventoryItem.TypeOfSchedule.TREE, notIndexedItem.getIndexationPlanType());

            ImportInventoryItem indexed1 = notIndexed.get(1);
            Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6", indexed1.getPid());
            Assert.assertEquals(true, indexed1.isPresentInProcessingIndex());
            Assert.assertEquals(ImportInventoryItem.TypeOfSchedule.OBJECT, indexed1.getIndexationPlanType());

            ImportInventoryItem indexed2 = notIndexed.get(2);
            Assert.assertEquals("uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6", indexed2.getPid());
            Assert.assertEquals(true, indexed2.isPresentInProcessingIndex());
            Assert.assertEquals(ImportInventoryItem.TypeOfSchedule.OBJECT, indexed2.getIndexationPlanType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
