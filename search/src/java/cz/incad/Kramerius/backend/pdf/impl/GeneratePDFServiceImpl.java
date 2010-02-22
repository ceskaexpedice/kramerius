package cz.incad.Kramerius.backend.pdf.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;

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
	public void generatePDF(String uuid, OutputStream os) throws IOException {
		try {
			Document doc = new Document();
			List<Element> pages = fedoraAccess.getPages(uuid);
			for (Element elm : pages) {
				String attribute = elm.getAttribute("rdf:resource");
				try {
					PIDParser pidParser= new PIDParser(attribute);
					pidParser.disseminationURI();
					String objectId = pidParser.getObjectId();
					String url = createDJVUUrl(objectId);
					Image imgElem = new Image(url);
					imgElem.setPosition(2, 2);
					doc.addPage(new Page(imgElem));

				} catch (LexerException e) {
					e.printStackTrace();
				}
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
