package cz.incad.kramerius.lp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.lp.guice.PDFModule;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.utils.DCUtils;

/**
 * Staticky export do pdf
 * @author pavels
 */
public class PDFExport {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(PDFExport.class.getName());
	
	
	public static void main(String[] args) throws IOException {
		// adresar pro vystup
		// uuid
		if (args.length == 2) {
			File outputFolder = new File(args[0]);
			if (!outputFolder.exists()) { outputFolder.mkdirs(); }
			//DecoratedOutputStream fos = new DecoratedOutputStream("static.pdf");
			Injector injector = Guice.createInjector(new PDFModule());
			FedoraAccess fa = injector.getInstance(FedoraAccess.class);
			FileOutputStream fos = null;
			try {
				File file = new File(outputFolder, DCUtils.titleFromDC(fa.getDC(args[1]))+".pdf");
				fos = new FileOutputStream(file);
				GeneratePDFService generatePDF = injector.getInstance(GeneratePDFService.class);
				generatePDF.fullPDFExport(args[1], fos);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				if (fos != null) fos.close();
			}
		}

	}
}
