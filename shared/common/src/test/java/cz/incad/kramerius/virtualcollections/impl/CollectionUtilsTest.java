package cz.incad.kramerius.virtualcollections.impl;

import org.json.JSONObject;

import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class CollectionUtilsTest extends TestCase {
    
    public void testCollectionToJSON() {
        Collection col = new Collection("vc:xxxx","label",false);
        col.addDescription(new Collection.Description("cs", "TEXT_cs", "cesky text"));
        col.addDescription(new Collection.Description("en", "TEXT_en", "english text"));
        JSONObject virtualCollectionTOJSON = CollectionUtils.virtualCollectionTOJSON(col);
        Assert.assertTrue(virtualCollectionTOJSON.getString("pid").equals("vc:xxxx"));
        Assert.assertTrue(virtualCollectionTOJSON.getString("label").equals("label"));
        Assert.assertFalse(virtualCollectionTOJSON.getBoolean("canLeave"));
    }
}
