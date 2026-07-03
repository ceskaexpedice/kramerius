package cz.incad.kramerius.services.workers.copy.cdk;

import cz.incad.kramerius.services.workers.copy.cdk.model.CDKNewConflictFeederItem;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKWorkerIndexedItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CDKCopyContextTest {

    @Test
    public void getAlreadyIndexedAsItemFindsIndexedItemByPid() {
        CDKWorkerIndexedItem first = indexedItem("id!uuid:1", "uuid:1");
        CDKWorkerIndexedItem second = indexedItem("id!uuid:2", "uuid:2");
        CDKCopyContext context = new CDKCopyContext(
                Collections.emptyList(),
                Arrays.asList(first, second),
                Collections.emptyList(),
                Collections.emptyList());

        Assert.assertSame(second, context.getAlreadyIndexedAsItem("uuid:2"));
        Assert.assertNull(context.getAlreadyIndexedAsItem("uuid:missing"));
    }

    @Test
    public void conflictRecordsCanBeAddedRemovedAndReplaced() {
        CDKCopyContext context = new CDKCopyContext(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        CDKNewConflictFeederItem first = newConflict("id!uuid:1", "uuid:1");
        CDKNewConflictFeederItem second = newConflict("id!uuid:2", "uuid:2");

        context.addConflictRecord(first);
        Assert.assertEquals(Collections.singletonList(first), context.getNewConflictRecords());

        context.setNewConflictRecords(new ArrayList<>(Collections.singletonList(second)));
        Assert.assertEquals(Collections.singletonList(second), context.getNewConflictRecords());

        context.removeConflictRecord(second);
        Assert.assertTrue(context.getNewConflictRecords().isEmpty());
    }

    private CDKWorkerIndexedItem indexedItem(String id, String pid) {
        Map<String, Object> document = new HashMap<>();
        document.put("pid", pid);
        document.put("compositeId", id);
        return new CDKWorkerIndexedItem(id, document);
    }

    private CDKNewConflictFeederItem newConflict(String id, String pid) {
        Map<String, Object> document = new HashMap<>();
        document.put("pid", pid);
        document.put("compositeId", id);
        return new CDKNewConflictFeederItem(id, Collections.singletonList(pid), document);
    }
}
