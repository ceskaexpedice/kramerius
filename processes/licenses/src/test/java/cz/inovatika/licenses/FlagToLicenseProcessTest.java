package cz.inovatika.licenses;

import cz.incad.kramerius.security.licenses.impl.embedded.cz.CzechEmbeddedLicenses;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FlagToLicenseProcessTest {

    @Test
    public void buildFilterQueriesUsesBatchedExactPidQueries() {
        List<String> queries = FlagToLicenseProcess.buildFilterQueries(
                "model:(monograph) AND accessibility:*",
                Arrays.asList("uuid:1", "uuid:2"),
                100);

        Assert.assertEquals(1, queries.size());
        Assert.assertEquals(
                "model:(monograph) AND accessibility:* AND (pid:\"uuid:1\" OR pid:\"uuid:2\")",
                queries.get(0));
    }

    @Test
    public void buildFilterQueriesSplitsThreeHundredPidsIntoBatches() {
        List<String> pids = new ArrayList<>();
        for (int i = 1; i <= 300; i++) {
            pids.add("uuid:" + i);
        }

        List<String> queries = FlagToLicenseProcess.buildFilterQueries(
                "model:(monograph) AND accessibility:*",
                pids,
                100);

        Assert.assertEquals(3, queries.size());
        Assert.assertEquals(100, countPidClauses(queries.get(0)));
        Assert.assertEquals(100, countPidClauses(queries.get(1)));
        Assert.assertEquals(100, countPidClauses(queries.get(2)));
        Assert.assertTrue(queries.get(0).contains("pid:\"uuid:1\""));
        Assert.assertTrue(queries.get(0).contains("pid:\"uuid:100\""));
        Assert.assertTrue(queries.get(1).contains("pid:\"uuid:101\""));
        Assert.assertTrue(queries.get(1).contains("pid:\"uuid:200\""));
        Assert.assertTrue(queries.get(2).contains("pid:\"uuid:201\""));
        Assert.assertTrue(queries.get(2).contains("pid:\"uuid:300\""));
    }

    @Test
    public void buildFilterQueriesWithoutPidsUsesBaseQueryOnly() {
        List<String> queries = FlagToLicenseProcess.buildFilterQueries(
                "model:(monograph) AND accessibility:*",
                null);

        Assert.assertEquals(Collections.singletonList("model:(monograph) AND accessibility:*"), queries);
    }

    @Test
    public void dnntoDoesNotSkipPublicLicenseAssignment() {
        String license = FlagToLicenseProcess.targetLicenseFor("public", Collections.singletonList("dnnto"));

        Assert.assertEquals(CzechEmbeddedLicenses.PUBLIC_LICENSE.getName(), license);
    }

    @Test
    public void publicLicenseSkipsPublicLicenseAssignment() {
        String license = FlagToLicenseProcess.targetLicenseFor(
                "public",
                Collections.singletonList(CzechEmbeddedLicenses.PUBLIC_LICENSE.getName()));

        Assert.assertNull(license);
    }

    @Test
    public void privateFlagAssignsOnsiteWhenNoTargetLicenseExists() {
        String license = FlagToLicenseProcess.targetLicenseFor("private", Collections.singletonList("dnnto"));

        Assert.assertEquals(CzechEmbeddedLicenses.ONSITE_LICENSE.getName(), license);
    }

    @Test
    public void onsiteLicenseSkipsOnsiteLicenseAssignment() {
        String license = FlagToLicenseProcess.targetLicenseFor(
                "private",
                Collections.singletonList(CzechEmbeddedLicenses.ONSITE_LICENSE.getName()));

        Assert.assertNull(license);
    }

    private int countPidClauses(String query) {
        return query.split("pid:").length - 1;
    }
}
