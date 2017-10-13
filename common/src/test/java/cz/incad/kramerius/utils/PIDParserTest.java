package cz.incad.kramerius.utils;

import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class PIDParserTest {

    @Test
    public void testPidParserWithStream() throws LexerException {
        PIDParser parser = new PIDParser(
                "uuid:814a6eb0-934c-11de-a77b-000d606f5dc6/DC");
        parser.objectPid();
        Assert.assertEquals(true, parser.isDatastreamPid());
        Assert.assertEquals("814a6eb0-934c-11de-a77b-000d606f5dc6",
                parser.getObjectId());
        Assert.assertEquals("DC", parser.getDataStream());
        Assert.assertEquals("uuid:814a6eb0-934c-11de-a77b-000d606f5dc6/DC",
                parser.getObjectPid());
    }

    @Test
    public void testPidParser() throws LexerException {
        // "info:fedora/uuid:814a6eb0-934c-11de-a77b-000d606f5dc6"
        PIDParser parser = new PIDParser(
                "info:fedora/uuid:814a6eb0-934c-11de-a77b-000d606f5dc6");
        parser.disseminationURI();
        parser.getObjectId();
        Assert.assertEquals("814a6eb0-934c-11de-a77b-000d606f5dc6",
                parser.getObjectId());
        Assert.assertEquals("uuid", parser.getNamespaceId());
        Assert.assertEquals(false, parser.isDatastreamPid());
    }

    @Test
    public void testPidParser2() throws LexerException {
        // "info:fedora/uuid:814a6eb0-934c-11de-a77b-000d606f5dc6"
        PIDParser parser = new PIDParser("info:fedora/model:monograph");
        parser.disseminationURI();
        Assert.assertEquals("monograph", parser.getObjectId());
        Assert.assertEquals("model", parser.getNamespaceId());
        Assert.assertEquals(false, parser.isDatastreamPid());
    }

    @Test
    public void testPidParser3() throws LexerException {
        try {
            PIDParser parser = new PIDParser(
                    " info:fedora/96fd202c-e640-11de-a504-001143e3f55c");
            parser.disseminationURI();
        } catch (LexerException e) {
            // ok
        }
    }

    @Test
    public void testPidParser4() throws LexerException {
        String pid = "uuid:MED00170455";
        boolean matches = pid
                .matches("^[Uu]{2}[Ii][Dd]:(([A-Za-z0-9])|-|\\.|~|_|(%[0-9A-F]{2}))+$");
        Assert.assertTrue(matches);
        PIDParser parser = new PIDParser(pid);
        parser.objectPid();
    }

    @Test
    public void testPidParserWitPage() throws LexerException {
        String pid = "uuid:3ee97ce8-e548-11e0-9867-005056be0007/@886";
        PIDParser parser = new PIDParser(pid);
        parser.objectPid();
        Assert.assertFalse(parser.isDatastreamPid());
        Assert.assertTrue(parser.getObjectPid().equals(pid));

    }

    @Test
    public void testPidParserNLK() throws LexerException {
        try {
            String pid = "uuid:MED00178583_test";
            PIDParser parser = new PIDParser(pid);
            parser.objectPid();
        } catch (LexerException le) {
            // ok
        }
    }

    @Test
    public void testCollectionPid() throws LexerException {
        PIDParser parser = new PIDParser(
                "vc:ebc58201-b12d-4be5-baa6-b0cdcf7f1ae3");
        parser.objectPid();
        Assert.assertEquals(false, parser.isDatastreamPid());
        Assert.assertEquals("ebc58201-b12d-4be5-baa6-b0cdcf7f1ae3",
                parser.getObjectId());
        Assert.assertEquals("vc:ebc58201-b12d-4be5-baa6-b0cdcf7f1ae3",
                parser.getObjectPid());
    }

    @Test
    public void testCollectionPidDisseminationUri() throws LexerException {
        PIDParser parser = new PIDParser(
                "info:fedora/vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646");
        parser.disseminationURI();
        Assert.assertEquals(false, parser.isDatastreamPid());
        Assert.assertEquals("64b95a45-6ead-4bf1-aa93-c31b0ccbf646",
                parser.getObjectId());
        Assert.assertEquals("vc:64b95a45-6ead-4bf1-aa93-c31b0ccbf646",
                parser.getObjectPid());
    }


    @Test
    public void testDonator() {
        try {
            String pid = "donator:norway";
            PIDParser parser = new PIDParser(pid);
            parser.objectPid();
        } catch (LexerException le) {
            // ok
            Assert.fail();
        }
        
    }

}
