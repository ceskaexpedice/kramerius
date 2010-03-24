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

import cz.incad.Kramerius.backend.impl.FedoraAccessImpl;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.pdf.model.Outline;
import cz.incad.kramerius.pdf.model.OutlineItem;
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

	@Test
	public void processRelsExt() throws ParserConfigurationException, SAXException, IOException {
//		InputStream stream = this.getClass().getResourceAsStream("rels-ext.xml");
//		Document document = XMLUtils.parseDocument(stream, true);	
		KConfiguration kConf = KConfiguration.getKConfiguration("/home/pavels/Programs/fedora-commons/tomcat/conf/kk.xml");
		
		System.out.println(kConf.getFedoraHost());
		System.out.println(kConf.getFedoraUser());
		System.out.println(kConf.getFedoraPass());
		final ArrayList<String> list = new ArrayList<String>();
		
		final FedoraAccessImpl fi = new FedoraAccessImpl(kConf);
		final StringBuffer buffer = new StringBuffer();
		
		Document relsExt = fi.getRelsExt("f77dba20-bd69-11dc-926d-000d606f5dc6");
		final Stack<OutlineItem> outlines = new Stack<OutlineItem>();
		final ArrayList<FedoraRelationship> relations = new ArrayList<FedoraRelationship>();
		
		
		fi.processRelsExt(relsExt, new RelsExtHandler() {
			
			int duringProcessingLevel = -1;
			
			@Override
			public void handle(Element elm, FedoraRelationship relation, Stack<Element> processingStack) {
				try {
					if (!relations.contains(relation)) { relations.add(relation); }
					int level = relations.indexOf(relation);
					if (duringProcessingLevel > level) { 
						outlines.pop(); 
					} else if (duringProcessingLevel < level) { 
						OutlineItem nItem = new OutlineItem();
						modifyElem(fi, elm, nItem);
						outlines.push(nItem);  
					} else if (duringProcessingLevel == level) {
						OutlineItem nItem = new OutlineItem();
						modifyElem(fi, elm, nItem);
						OutlineItem poped = outlines.peek();
						poped.addItem(nItem);
					}
					duringProcessingLevel = level;
				} catch (DOMException e) {
					e.printStackTrace();
//				} catch (LexerException e) {
//					e.printStackTrace();
				} catch (LexerException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			private void modifyElem(final FedoraAccessImpl fi, Element elm,
					OutlineItem poped) throws LexerException, IOException {
				String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
				PIDParser pidParse = new PIDParser(pid);
				pidParse.disseminationURI();
				String objectId = pidParse.getObjectId();

				Document dc = fi.getDC(objectId);
				String titleFromDC = DCUtils.titleFromDC(dc);
				poped.setTitle(titleFromDC);
			}
			
			@Override
			public boolean accept(FedoraRelationship relation) {
				return true;
			}
		});
		//System.out.println(outlines);
		System.out.println(relations);
		OutlineItem root = null;
		while(!outlines.isEmpty()) { root = outlines.pop(); }
		
	}
}
