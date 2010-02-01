package cz.i.kramerius.gwtviewers.server.pid;

import junit.framework.TestCase;

public class PIDParserTest extends TestCase {

//	public void testParser() throws LexerException {
//		PIDParser parser = new PIDParser("demo:1");
//		parser.objectPid();
//	}
//	
//	public void testParser2() throws LexerException {
//		PIDParser parser = new PIDParser("demo:A-B.C_D%3AE");
//		parser.objectPid();
//	}
//	
//	public void testParser3() throws LexerException {
//		PIDParser parser = new PIDParser("demo:MyFedoraDigitalObject");
//		parser.objectPid();
//	}
//
//	public void testParser4() throws LexerException {
//		PIDParser parser = new PIDParser("demo%3aMyFedoraDigitalObject");
//		parser.objectPid();
//	}
	
	public void testParser5() throws LexerException {
		//info:fedora/uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6
		PIDParser parser = new PIDParser("info:fedora/uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6");
		parser.disseminationURI();
		System.out.println(parser.getNamespaceId());
		System.out.println(parser.getObjectId());
		
	}
}
