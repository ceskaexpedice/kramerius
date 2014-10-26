package cz.incad.kramerius.utils.handle;

import junit.framework.Assert;

import org.junit.Test;

public class DisectHandleTest {

    @Test
    public void testDisectHandle() {
        String url="http://localhost:8080/search/handle/uuid:045b1250-7e47-11e0-add1-000d606f5dc6";
        String handle = DisectHandle.disectHandle(url);
        Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6",handle);
    }

    @Test
    public void testDisectHandleWithPage() {
        String url="http://localhost:8080/search/handle/uuid:045b1250-7e47-11e0-add1-000d606f5dc6/@2";
        String handle = DisectHandle.disectHandle(url);
        Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6/@2",handle);
    }


    @Test
    public void testDisectHandleClient() {
        String url="http://localhost:8080/client/handle/uuid:045b1250-7e47-11e0-add1-000d606f5dc6";
        String handle = DisectHandle.disectHandle(url);
        Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6",handle);
    }

}
