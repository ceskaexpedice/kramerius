package cz.incad.Kramerius.backend.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Service for generating PDF
 * @author pavels
 */
public interface GeneratePDFService {


	/**
	 * Generate pdf
	 * @param uuids Pages to PDF document
	 * @param os Outputstream
	 * @throws IOException
	 */
	//public void generatePDF(String parentUuid,  OutputStream os) throws IOException;
	
	//public void generatePDFOutlined(String parentUUID, List<String> uuids, OutputStream os) throws IOException;
	
	public void generatePDFOutlined(String parentUUID, OutputStream os) throws IOException;

	public void generatePDFOutlined(String parentUUIDm,List<String> uuids, String titlePage, OutputStream os) throws IOException;
}

