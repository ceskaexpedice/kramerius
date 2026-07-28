package cz.incad.kramerius.services.workers.copy.cdk;

import cz.incad.kramerius.services.workers.copy.cdk.model.CDKExistingConflictFeederItem;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CDKCopyMigrationIndexFeederConflictTest {

    @Test
    public void findConflictDetectsRootChange() {
        Map<String, Object> indexed = doc(
                "uuid:child",
                "uuid:root-old!uuid:child",
                "uuid:root-old",
                "monograph",
                "monograph/page",
                "uuid:parent-old");

        Map<String, Object> source = doc(
                "uuid:child",
                "uuid:root-new!uuid:child",
                "uuid:root-new",
                "monograph",
                "monograph/page",
                "uuid:parent-new");

        Assert.assertTrue(CDKCopyMigrationIndexFeeder.findConflict(indexed, source));
    }

    @Test
    public void findConflictDetectsModelChangeEvenWithSameCompositeId() {
        Map<String, Object> indexed = doc(
                "uuid:child",
                "uuid:root!uuid:child",
                "uuid:root",
                "monograph",
                "monograph/page",
                "uuid:parent");

        Map<String, Object> source = doc(
                "uuid:child",
                "uuid:root!uuid:child",
                "uuid:root",
                "periodical",
                "periodical/periodicalitem",
                "uuid:parent");

        Assert.assertTrue(CDKCopyMigrationIndexFeeder.findConflict(indexed, source));
    }

    @Test
    public void findIndexConflictMarksSourceMismatchAsConflict() {
        IterationItem sourceItem = new IterationItem("uuid:child", "source", sourceDoc(
                "uuid:child",
                "uuid:root-new!uuid:child",
                "uuid:root-new",
                "periodical",
                "periodical/periodicalitem",
                "uuid:parent-new"));

        Map<String, Object> indexed = doc(
                "uuid:child",
                "uuid:root-old!uuid:child",
                "uuid:root-old",
                "monograph",
                "monograph/page",
                "uuid:parent-old");

        List<CDKExistingConflictFeederItem> conflicts = CDKCopyMigrationIndexFeeder.findIndexConflict(
                "pid",
                Collections.singletonList(sourceItem),
                Collections.singletonList(indexed));

        Assert.assertEquals(1, conflicts.size());
        Assert.assertEquals("uuid:child", conflicts.get(0).getPid());
        Assert.assertTrue(conflicts.get(0).isConflict());
        Assert.assertTrue(conflicts.get(0).getCompositeIds().contains("uuid:root-old!uuid:child"));
        Assert.assertTrue(conflicts.get(0).getCompositeIds().contains("uuid:root-new!uuid:child"));
    }

    @Test
    public void findIndexConflictDetectsDuplicateCompositeIds() {
        IterationItem sourceItem = new IterationItem("uuid:child", "source", sourceDoc(
                "uuid:child",
                "uuid:root!uuid:child",
                "uuid:root",
                "monograph",
                "monograph/page",
                "uuid:parent"));

        Map<String, Object> indexedOne = doc(
                "uuid:child",
                "uuid:root!uuid:child",
                "uuid:root",
                "monograph",
                "monograph/page",
                "uuid:parent");
        Map<String, Object> indexedTwo = doc(
                "uuid:child",
                "uuid:other-root!uuid:child",
                "uuid:other-root",
                "monograph",
                "monograph/page",
                "uuid:parent");

        List<CDKExistingConflictFeederItem> conflicts = CDKCopyMigrationIndexFeeder.findIndexConflict(
                "pid",
                Collections.singletonList(sourceItem),
                new ArrayList<>(Arrays.asList(indexedOne, indexedTwo)));

        Assert.assertEquals(1, conflicts.size());
        Assert.assertTrue(conflicts.get(0).isConflict());
        Assert.assertEquals(2, conflicts.get(0).getCompositeIds().size());
    }

    private Map<String, Object> doc(String pid, String compositeId, String rootPid, String model, String ownModelPath, String ownParentPid) {
        Map<String, Object> document = new HashMap<>();
        document.put("pid", pid);
        document.put("compositeId", compositeId);
        document.put("root.pid", rootPid);
        document.put("model", model);
        document.put("own_model_path", ownModelPath);
        document.put("own_parent.pid", ownParentPid);
        return document;
    }

    private Map<String, Object> sourceDoc(String pid, String compositeId, String rootPid, String model, String ownModelPath, String ownParentPid) {
        return doc(pid, compositeId, rootPid, model, ownModelPath, ownParentPid);
    }
}
