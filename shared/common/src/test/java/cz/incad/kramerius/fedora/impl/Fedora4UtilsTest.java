package cz.incad.kramerius.fedora.impl;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pstastny on 10/10/2017.
 */
public class Fedora4UtilsTest extends TestCase  {
    // TODO AK_NEW

    public void testNormalizePath() {
//        List<String> path = Fedora4Utils.normalizePath("uuid:69d79410-490a-11de-9d6a-000d606f5dc6");
//        System.out.println(path);
//        Assert.assertTrue(path.size() == 4);
//        Assert.assertTrue(path.get(0).equals("data"));
//        Assert.assertTrue(path.get(1).equals("69d"));
//        Assert.assertTrue(path.get(2).equals("794"));
//        Assert.assertTrue(path.get(3).equals("10490a11de9d6a000d606f5dc6"));
//
//        path = Fedora4Utils.normalizePath("vc:69d79410-490a-11de-9d6a-000d606f5dc6");
//        Assert.assertTrue(path.size() == 2);
//        Assert.assertTrue(path.get(0).equals("collections"));
//
//        path = Fedora4Utils.normalizePath("model:page");
//        Assert.assertTrue(path.size() == 2);
//        Assert.assertTrue(path.get(0).equals("model"));
//
//        path = Fedora4Utils.normalizePath("donator:norway");
//        System.out.println(path);
    }

    public void testPath() {
//        List<String> path = Fedora4Utils.normalizePath("uuid:69d79410-490a-11de-9d6a-000d606f5dc6");
//        String path1 = Fedora4Utils.path(path);
//        System.out.println(path1);
//        Assert.assertEquals(path1,"/data/69d/794/10490a11de9d6a000d606f5dc6");
//
//        path = Fedora4Utils.normalizePath("vc:69d79410-490a-11de-9d6a-000d606f5dc6");
//        path1 = Fedora4Utils.path(path);
//        Assert.assertEquals(path1,"/collections/69d79410-490a-11de-9d6a-000d606f5dc6");
//
//        path = Fedora4Utils.normalizePath("model:page");
//        path1 = Fedora4Utils.path(path);
//        Assert.assertEquals(path1, "/model/page");
    }

    public void testLinks() {
//        String link = "http://localhost:18080/rest/data/fce/78e/00a/c07/456/79b/80b/2c2/a85/f4d/fb/TEXT_OCR";
//        List<String> parts = Fedora4Utils.link(link);
//        List<String> expectedParts = Arrays.asList("data", "fce", "78e", "00a", "c07", "456", "79b", "80b", "2c2", "a85", "f4d","fb", "TEXT_OCR");
//        System.out.println(parts);
//        Assert.assertTrue(parts.size() == expectedParts.size());
//        for (int i = 0,ll=parts.size(); i <ll ; i++) {
//            System.out.println(parts.get(i));
//            Assert.assertEquals(parts.get(i),expectedParts.get(i));
//        }
    }
}

