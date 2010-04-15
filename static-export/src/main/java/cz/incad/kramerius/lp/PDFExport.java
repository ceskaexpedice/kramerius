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
		if (args.length == 3) {
			LOGGER.info("Parameters "+args[0]+", "+args[1]+", "+args[2]);

			String outputFolderName = args[0];
			Medium medium = Medium.valueOf(args[1]);
			String uuid = args[2];

			File uuidFolder = new File(getTmpDir(), uuid);
			if (uuidFolder.exists()) { uuidFolder.delete(); }
			
			Injector injector = Guice.createInjector(new PDFModule());
			generatePDFs(uuid, uuidFolder, injector);
			createFSStructure(uuidFolder, new File(outputFolderName), medium);
		}
	}

	private static void createFSStructure(File pdfsFolder, File outputFodler, Medium medium) {
		int pocitadlo = 0;
		long bytes = 0;
		File currentFolder = createFolder(outputFodler, medium, ++pocitadlo);
		File[] listFiles = pdfsFolder.listFiles();
		for (File file : listFiles) {
			if ((bytes+file.length()) > medium.getSize()) {
				currentFolder = createFolder(outputFodler, medium, ++pocitadlo);
				bytes = 0;
			}
			file.renameTo(new File(currentFolder, file.getName()));
			bytes += file.length();
		}
	}

	private static File createFolder(File outputFodler, Medium medium, int pocitadlo) {
		return new File(outputFodler, medium.name()+"_"+pocitadlo);
	}


	private static void generatePDFs(String uuid, File uuidFolder, Injector injector) {
		try {
			if (!uuidFolder.exists()) { 
				uuidFolder.mkdirs(); 
			} else {
					File[] files = uuidFolder.listFiles(); 
					if (files != null) {
						for (File file : files) { file.deleteOnExit(); }
					}
			}
			FedoraAccess fa = injector.getInstance(FedoraAccess.class);
			GeneratePDFService generatePDF = injector.getInstance(GeneratePDFService.class);
			LOGGER.info("fedoraAccess.getDC("+uuid+")");
			Document dc = fa.getDC(uuid);
			LOGGER.info("dcUtils.titleFromDC("+dc+")");
			String title = DCUtils.titleFromDC(dc);
			LOGGER.info("title is "+title);
			GenerateController controller = new GenerateController(uuidFolder, title);
			generatePDF.fullPDFExport(uuid, controller, controller);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private static File getTmpDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}
}
