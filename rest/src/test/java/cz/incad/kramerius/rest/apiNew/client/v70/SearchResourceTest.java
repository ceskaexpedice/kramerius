package cz.incad.kramerius.rest.apiNew.client.v70;

import org.junit.Assert;
import org.junit.Test;

public class SearchResourceTest {

    @Test
    public void stripsNumericBoostsFromFilterQuery() {
        String fq = "(model:monograph^8 OR model:monographunit^2 OR model:periodical^10 OR model:map^2 OR model:graphic^2 OR model:manuscript^2 OR model:archive^2 OR model:sheetmusic^2 OR model:soundrecording^2 OR model:convolute^2 OR model:collection^2 OR model:page^0.001)";

        Assert.assertEquals(
                "(model:monograph OR model:monographunit OR model:periodical OR model:map OR model:graphic OR model:manuscript OR model:archive OR model:sheetmusic OR model:soundrecording OR model:convolute OR model:collection OR model:page)",
                SearchResource.stripBoostsFromFilterQuery(fq));
    }

    @Test
    public void keepsFilterQueryWithoutBoostsUnchanged() {
        String fq = "{!tag=model}model:\"page\"";

        Assert.assertEquals(fq, SearchResource.stripBoostsFromFilterQuery(fq));
    }

    @Test
    public void leavesEscapedCaretsUntouched() {
        String fq = "title:\\^special^2";

        Assert.assertEquals("title:\\^special", SearchResource.stripBoostsFromFilterQuery(fq));
    }
}
