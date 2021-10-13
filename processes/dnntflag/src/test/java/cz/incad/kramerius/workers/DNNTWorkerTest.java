package cz.incad.kramerius.workers;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class DNNTWorkerTest {

    @Test
    public void testWorker1() {
        DNNTWorker worker = new DNNTLabelWorker(
                "uuid:c69e8df0-8ce5-11e9-b66a-005056825209",
                null,
                null,
                "Covid",
                true
                );

        List<String> pids = worker.solrPidParents("uuid:c69e8df0-8ce5-11e9-b66a-005056825209", Arrays.asList("uuid:f6bd1dc0-890d-11e9-b724-005056827e52/uuid:a5f3c7a0-6a6c-11e9-9d6e-005056827e51/uuid:c69e8df0-8ce5-11e9-b66a-005056825209/uuid:5d22e150-6a6c-11e9-bcdf-005056827e52/uuid:3bcc2663-a81f-4c31-b2a3-7c4d75f66bf7"));
        Assert.assertTrue(pids.size() == 2);
        Assert.assertTrue(pids.contains("uuid:f6bd1dc0-890d-11e9-b724-005056827e52"));
        Assert.assertTrue(pids.contains("uuid:a5f3c7a0-6a6c-11e9-9d6e-005056827e51"));
    }

    @Test
    public void testWorker2() {
        DNNTWorker worker = new DNNTLabelWorker(
                "uuid:c69e8df0-8ce5-11e9-b66a-005056825209",
                null,
                null,
                "Covid",
                true
        );

        List<String> pids = worker.solrPidParents("uuid:c69e8df0-8ce5-11e9-b66a-005056825209", Arrays.asList(
                "uuid:f6bd1dc0-890d-11e9-b724-005056827e52/uuid:a5f3c7a0-6a6c-11e9-9d6e-005056827e51/uuid:c69e8df0-8ce5-11e9-b66a-005056825209/uuid:5d22e150-6a6c-11e9-bcdf-005056827e52/uuid:3bcc2663-a81f-4c31-b2a3-7c4d75f66bf7",
                "uuid:f6bd1dc0-890d-11e9-b724-005056827bbb/uuid:a5f3c7a0-6a6c-11e9-9d6e-005056827bbb/uuid:c69e8df0-8ce5-11e9-b66a-005056825209/uuid:5d22e150-6a6c-11e9-bcdf-005056827bbb/uuid:3bcc2663-a81f-4c31-b2a3-7c4d75f66bbb"
        ));
        Assert.assertTrue(pids.size() == 4);
        Assert.assertTrue(pids.contains("uuid:f6bd1dc0-890d-11e9-b724-005056827e52"));
        Assert.assertTrue(pids.contains("uuid:a5f3c7a0-6a6c-11e9-9d6e-005056827e51"));

        Assert.assertTrue(pids.contains("uuid:f6bd1dc0-890d-11e9-b724-005056827bbb"));
        Assert.assertTrue(pids.contains("uuid:a5f3c7a0-6a6c-11e9-9d6e-005056827bbb"));
    }
}
