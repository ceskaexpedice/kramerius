package cz.incad.kramerius.processes.impl;

import junit.framework.Assert;

import org.junit.Test;

public class IterateNonParametrizedTest {

    @Test
    public void testTemplate(){
        String value = IterateNonParametrized.template("nic", "uuid:xxx","20", "page");
        Assert.assertEquals("nic", value);
        
        value = IterateNonParametrized.template("$pid$-AAA-$model$", "uuid:xxx","20", "page");
        Assert.assertEquals("uuid:xxx-AAA-page", value);

        value = IterateNonParametrized.template("$pid$-AAA-$model$[$index$]", "uuid:xxx","20", "page");
        Assert.assertEquals("uuid:xxx-AAA-page[20]", value);
    }
}
