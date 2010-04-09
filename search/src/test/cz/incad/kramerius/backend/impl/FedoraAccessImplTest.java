package cz.incad.kramerius.backend.impl;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;


public class FedoraAccessImplTest {
	
//	@Test
//	public void getPagesTest() throws IOException, ParserConfigurationException, SAXException {
//		InputStream stream = this.getClass().getResourceAsStream("rels-ext.xml");
//		Document document = XMLUtils.parseDocument(stream);	
//		FedoraAccessImpl fi = new FedoraAccessImpl(null);
//		List<Element> pages = fi.getPages(null, document.getDocumentElement());
//		assertNotNull(pages);
//		assertTrue(!pages.isEmpty());
//		assertTrue(pages.size() == 16);
//	}

	
}
