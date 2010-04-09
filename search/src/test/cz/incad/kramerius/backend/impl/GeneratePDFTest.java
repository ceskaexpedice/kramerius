package cz.incad.kramerius.backend.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.pdf.GeneratePDFService;


public class GeneratePDFTest extends TestCase {

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
		instance.fullPDFExport("0eaa6730-9068-11dd-97de-000d606f5dc6",fos);
		//instance.dynamicPDFExport(Arrays.asList("0eaa6730-9068-11dd-97de-000d606f5dc6"), "430d7f60-b03b-11dd-82fa-000d606f5dc6", "4314ab50-b03b-11dd-89db-000d606f5dc6", "430d7f60-b03b-11dd-82fa-000d606f5dc6", fos);
		//instance.fullPDFExport("974d3b6c-e640-11de-a504-001143e3f55c", fos);
		
		fos.close();
		
		// item
		//instance.generatePDFOutlined("046a78f2-32f0-11de-992b-00145e5790ea", bos);
		Assert.assertNotNull(instance);
		
	}
}
