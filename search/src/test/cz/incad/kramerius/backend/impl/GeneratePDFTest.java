package cz.incad.kramerius.backend.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.pdf.Break;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.OutputStreams;


public class GeneratePDFTest extends TestCase {
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GeneratePDFTest.class.getName());
	
	class Oss implements OutputStreams {

		private int pocitadlo = 0;
		
		@Override
		public OutputStream newOutputStream() {
			try {
				pocitadlo += 1;
				return new FileOutputStream(new File("_"+pocitadlo+".pdf"));
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				return null;
			}
		}
	}
	
	static class GenerateController implements Break, OutputStreams {
		static int VELIKOST = 2*(1 << 20);

		private File curFile;
		private DecoratedOutputStream currentDos;
		private int pocitadlo = 1;
		
		@Override
		public OutputStream newOutputStream() throws IOException {
			this.curFile = new File("_"+pocitadlo+"_.pdf");
			this.currentDos = new DecoratedOutputStream(this.curFile);
			this.pocitadlo += 1;
			return this.currentDos;
		}

		@Override
		public boolean broken(String uuid) {
			if (this.currentDos.getActualSize() >= VELIKOST) return true;
			return false;
		}
	}
	
	@Test
	public void testGen() throws IOException {
		//ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DecoratedOutputStream fos = new DecoratedOutputStream("static.pdf");
		Injector injector = Guice.createInjector(new TestModule());
		GeneratePDFService instance = injector.getInstance(GeneratePDFService.class);
		//instance.generatePDFOutlined("02203ad6-32f0-11de-992b-00145e5790ea",bos);
		//uuid:0225e040-32f0-11de-992b-00145e5790ea
		//instance.generatePDFOutlined("0225e040-32f0-11de-992b-00145e5790ea",bos);
		//instance.generatePDFOutlined("22753524-32f0-11de-992b-00145e5790ea",bos);
		//instance.generatePDFOutlined("0225e040-32f0-11de-992b-00145e5790ea",fos);
		
		//430d7f60-b03b-11dd-82fa-000d606f5dc6&scaledHeight=600
		//4314ab50-b03b-11dd-89db-000d606f5dc6&scaledHeight=600
		//instance.fullPDFExport("0eaa6730-9068-11dd-97de-000d606f5dc6",fos);
		//instance.dynamicPDFExport(Arrays.asList("0eaa6730-9068-11dd-97de-000d606f5dc6"), "430d7f60-b03b-11dd-82fa-000d606f5dc6", "4314ab50-b03b-11dd-89db-000d606f5dc6", "430d7f60-b03b-11dd-82fa-000d606f5dc6", fos);
		//instance.fullPDFExport("974d3b6c-e640-11de-a504-001143e3f55c", fos);
		//instance.fullPDFExport("e3a6a694-2eda-48c9-97f9-76bcadfb291d", fos);
		GenerateController controller = new GenerateController();
		//instance.fullPDFExport("966dfeb3-e640-11de-a504-001143e3f55c", controller, controller);
		//instance.fullPDFExport("9a2c4008-e640-11de-a504-001143e3f55c", fos);
		//instance.fullPDFExport("9a2c671a-e640-11de-a504-001143e3f55c", controller, controller);
		//instance.fullPDFExport("966dfeb3-e640-11de-a504-001143e3f55c", controller, controller);
		
		//http://194.108.215.227:8080/fedora/get/uuid:e3a6a694-2eda-48c9-97f9-76bcadfb291d/RELS-EXT

		fos.close();
		
		// item
		//instance.generatePDFOutlined("046a78f2-32f0-11de-992b-00145e5790ea", bos);
		Assert.assertNotNull(instance);
	}
}
