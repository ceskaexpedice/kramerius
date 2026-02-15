package cz.inovatika.dochub.impl;

import cz.incad.kramerius.security.licenses.limits.LimitInterval;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMockBuilder;

public class FileUsageCounterImplTest {

    private List<Path> generateDynamicFiles() {
        List<Path> generatedFiles = new ArrayList<>();
        Instant now = Instant.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss.nnnnnnnnn'Z'")
                .withZone(ZoneOffset.UTC);

        for (int i = 0; i < 10; i++) {
            Instant fileInstant = now.minus(i, ChronoUnit.MINUTES);
            String fileName = formatter.format(fileInstant) + ".log";
            generatedFiles.add(Path.of(fileName));
        }

        return generatedFiles;
    }

    @Test
    public void test10PerDay() throws IOException {
        String userdir = System.getProperty("user.dir");
        List<Path> files = generateDynamicFiles();
        FileUsageCounterImpl fileCount = createMockBuilder(FileUsageCounterImpl.class)
                .withConstructor(Path.class)
                .withArgs(Path.of(userdir))
                .addMockedMethod("streamFiles")
                .addMockedMethod("auditFolderExists")
                .createMock();


        EasyMock.expect(fileCount.streamFiles(EasyMock.anyObject(Path.class))).andAnswer(()-> {return files.stream();}).anyTimes();
        EasyMock.expect(fileCount.auditFolderExists(EasyMock.anyObject(Path.class))).andReturn(true).anyTimes();
        EasyMock.replay(fileCount);
        long usageCount = fileCount.getUsageCount("user", "uuid:xxxx", LimitInterval.PER_DAY, 1);
        Assert.assertEquals(10, usageCount);
    }

    @Test
    public void test5PerMinute() throws IOException {
        String userdir = System.getProperty("user.dir");
        List<Path> files = generateDynamicFiles();
        FileUsageCounterImpl fileCount = createMockBuilder(FileUsageCounterImpl.class)
                .withConstructor(Path.class)
                .withArgs(Path.of(userdir))
                .addMockedMethod("streamFiles")
                .addMockedMethod("auditFolderExists")
                .createMock();


        EasyMock.expect(fileCount.streamFiles(EasyMock.anyObject(Path.class))).andAnswer(()-> {return files.stream();}).anyTimes();
        EasyMock.expect(fileCount.auditFolderExists(EasyMock.anyObject(Path.class))).andReturn(true).anyTimes();
        EasyMock.replay(fileCount);
        long usageCount = fileCount.getUsageCount("user", "uuid:xxxx", LimitInterval.PER_MINUTE, 5);
        Assert.assertEquals(5, usageCount);
    }

    @Test
    public void test2PerMinute() throws IOException {
        String userdir = System.getProperty("user.dir");
        List<Path> files = generateDynamicFiles();
        FileUsageCounterImpl fileCount = createMockBuilder(FileUsageCounterImpl.class)
                .withConstructor(Path.class)
                .withArgs(Path.of(userdir))
                .addMockedMethod("streamFiles")
                .addMockedMethod("auditFolderExists")
                .createMock();


        EasyMock.expect(fileCount.streamFiles(EasyMock.anyObject(Path.class))).andAnswer(()-> {return files.stream();}).anyTimes();
        EasyMock.expect(fileCount.auditFolderExists(EasyMock.anyObject(Path.class))).andReturn(true).anyTimes();
        EasyMock.replay(fileCount);
        long usageCount = fileCount.getUsageCount("user", "uuid:xxxx", LimitInterval.PER_MINUTE, 2);
        Assert.assertEquals(2, usageCount);
    }

}
