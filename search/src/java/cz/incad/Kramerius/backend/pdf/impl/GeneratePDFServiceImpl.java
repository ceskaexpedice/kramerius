package cz.incad.Kramerius.backend.pdf.impl;

import static cz.incad.kramerius.FedoraNamespaces.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Document;
import com.qbizm.kramerius.ext.ontheflypdf.edi.EdiException;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Image;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Page;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.PdfDocumentGenerator;

import cz.incad.Kramerius.backend.pdf.GeneratePDFService;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class GeneratePDFServiceImpl implements GeneratePDFService {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GeneratePDFServiceImpl.class.getName());
	
	@Inject
	FedoraAccess fedoraAccess;
	@Inject
	KConfiguration configuration;
	
	
	@Override
	public void generatePDF(String parentUUID, List<String> uuids, OutputStream os)
			throws IOException {
		try {
			Document doc = new Document();
//			if (!uuids.isEmpty()) {
//				doc.addPage(createFirstPage(parentUUID));
//			}
			for (String uuid : uuids) {
				String url = createDJVUUrl(uuid);
				Image imgElem = new Image(url);
				imgElem.setPosition(2, 2);
				doc.addPage(new Page(imgElem));
			}
			if (doc.getPagesCount() > 0) {
				PdfDocumentGenerator pdfDocumentGenerator = new PdfDocumentGenerator(doc);
				pdfDocumentGenerator.setDisector(new KrameriusImageFormatDisector());
				pdfDocumentGenerator.generateDocument(os);
			}
		} catch (EdiException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
	}

	@Override
	public void generatePDF(String uuid, OutputStream os) throws IOException {
		List<Element> pages = fedoraAccess.getPages(uuid,false);
		for (Element element : pages) {
			
		}
	}

	private Page createFirstPage(String uuid) throws IOException {
		org.w3c.dom.Document biblioMods = this.fedoraAccess.getDC(uuid);
		Element root = biblioMods.getDocumentElement();
		Map<String, List<String>> stModel = new HashMap<String, List<String>>();
		NodeList nlist = root.getChildNodes();
		for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
			Node item = nlist.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				String name = item.getNodeName();
				String value = item.getTextContent();
				List<String> vals = stModel.get(name);
				if (vals == null) {
					vals = new ArrayList<String>();
					stModel.put(name, vals);
				}
				vals.add(value);
			}
		}
		
		
		return null;
	}


	private String createThumbUrl(String objectId) {
		String imgUrl = this.configuration.getThumbServletUrl() +"?uuid="+objectId+"&scale=1.0&rawdata=true";
		return imgUrl;
	}

	private String createDJVUUrl(String objectId) {
		String imgUrl = this.configuration.getDJVUServletUrl() +"?uuid="+objectId+"";
		return imgUrl;
	}


	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}

	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}

	public KConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(KConfiguration configuration) {
		this.configuration = configuration;
	}

	
}
