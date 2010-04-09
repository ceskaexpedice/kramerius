package cz.incad.kramerius.lp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.w3c.dom.Document;

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
		if (args.length == 3) {
			System.out.println("Parameters "+args[0]+", "+args[1]+","+ args[2]);
			System.setProperty(PDFModule.KK_PATH, args[1]);
			File outputFolder = new File(args[0]);
			if (!outputFolder.exists()) { outputFolder.mkdirs(); }
			//DecoratedOutputStream fos = new DecoratedOutputStream("static.pdf");
			Injector injector = Guice.createInjector(new PDFModule());
			FedoraAccess fa = injector.getInstance(FedoraAccess.class);
			FileOutputStream fos = null;
			try {
				LOGGER.info("fedoraAccess.getDC("+args[2]+")");
				Document dc = fa.getDC(args[2]);
				LOGGER.info("dcUtils.titleFromDC("+dc+")");
				String title = DCUtils.titleFromDC(dc);
				LOGGER.info("title is "+title);
				File file = new File(outputFolder, title+".pdf");
				if (file.exists()) file.delete();
				boolean created = file.createNewFile();
				if (!created) throw new IllegalArgumentException("cannot create file '"+file.getAbsolutePath()+"'");
				LOGGER.info("created file "+file.getAbsolutePath());
				fos = new FileOutputStream(file);
				GeneratePDFService generatePDF = injector.getInstance(GeneratePDFService.class);
				LOGGER.info("calling fullExport method to service paremters = ("+args[2]+","+fos+")");
				generatePDF.fullPDFExport(args[2], fos);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				if (fos != null) fos.close();
			}
		}

	}
}
