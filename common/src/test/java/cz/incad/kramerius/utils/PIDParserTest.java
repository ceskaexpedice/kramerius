package cz.incad.kramerius.utils;

import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class PIDParserTest {

	@Test
	public void testPidParser() throws LexerException {
		//"info:fedora/uuid:814a6eb0-934c-11de-a77b-000d606f5dc6"
		PIDParser parser = new PIDParser("info:fedora/uuid:814a6eb0-934c-11de-a77b-000d606f5dc6");
		parser.disseminationURI();
		parser.getObjectId();
		Assert.assertEquals("814a6eb0-934c-11de-a77b-000d606f5dc6", parser.getObjectId());
		Assert.assertEquals("uuid", parser.getNamespaceId());
	}
	
	@Test
	public void testPidParser2() throws LexerException {
		//"info:fedora/uuid:814a6eb0-934c-11de-a77b-000d606f5dc6"
		PIDParser parser = new PIDParser("info:fedora/model:monograph");
		parser.disseminationURI();
		Assert.assertEquals("monograph", parser.getObjectId());
		Assert.assertEquals("model", parser.getNamespaceId());
	}
	
}
