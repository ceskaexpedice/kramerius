package cz.incad.kramerius.pdf;

import java.io.FileOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class PDFMain {

    public static void main(String arg[])throws Exception {
		Document document=new Document();
		PdfWriter.getInstance(document,new FileOutputStream("hello.pdf"));
		document.open();  
		document.add(new Paragraph("Hello Pdf"));
		document.close(); 
    }
}
