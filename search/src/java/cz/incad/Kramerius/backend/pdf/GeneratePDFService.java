package cz.incad.Kramerius.backend.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Service for generating PDF
 * @author pavels
 */
public interface GeneratePDFService {

	/**
	 * Generate pdf 
	 * @param uuid  UUID of object
	 * @param os Outputstream to generate pdf
	 * @throws IOException 
	 */
	public void generatePDF(String uuid, OutputStream os) throws IOException;
}
