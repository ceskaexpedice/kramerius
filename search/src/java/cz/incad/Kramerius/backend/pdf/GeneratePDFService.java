package cz.incad.Kramerius.backend.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import cz.incad.Kramerius.backend.pdf.impl.pdfpages.AbstractRenderedDocument;

/**
 * Service for generating PDF
 * @author pavels
 */
public interface GeneratePDFService {

	public void generatePDFOutlined(String parentUUID, OutputStream os) throws IOException;

	//public void generatePDFOutlined(String parentUUIDm,List<String> uuids, String titlePage, OutputStream os) throws IOException;
	
	public void generatePDFOutlined(AbstractRenderedDocument doc, String parentUUID, OutputStream os) throws IOException;
}

