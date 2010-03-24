package cz.incad.kramerius.pdf;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfAction;
import java.io.FileOutputStream;

public class Outline {

	public Outline() throws Exception{

		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, 
			new FileOutputStream("outline.pdf"));
		document.open();

		// Code 1
		document.add(new Chunk("Chapter 1")
			.setLocalDestination("1"));		
		document.newPage();
		
		document.add(new Chunk("Chapter 2")
			.setLocalDestination("2"));
		document.add(new Paragraph(new Chunk("Sub 2.1")
			.setLocalDestination("2.1")));
		document.add(new Paragraph(new Chunk("Sub 2.2")
			.setLocalDestination("2.2")));				
		document.newPage();
		
		document.add(new Chunk("Chapter 3")
			.setLocalDestination("3"));
		
		// Code 2
		PdfContentByte cb = writer.getDirectContent();
		PdfOutline root = cb.getRootOutline();
		
		// Code 3
		PdfOutline oline1 = new PdfOutline(root, 
			PdfAction.gotoLocalPage("1", false),"Chapter 1");
		
		PdfOutline oline2 = new PdfOutline(root, 
			PdfAction.gotoLocalPage("2", false),"Chapter 2");
		oline2.setOpen(false);
		PdfOutline oline2_1 = new PdfOutline(oline2, 
			PdfAction.gotoLocalPage("2.1", false),"Sub 2.1");
		PdfOutline oline2_2 = new PdfOutline(oline2, 
			PdfAction.gotoLocalPage("2.2", false),"Sub 2.2");
		
		PdfOutline oline3 = new PdfOutline(root, 
			PdfAction.gotoLocalPage("3", false),"Chapter 3");
		
		document.close();
	}
	
	public static void main(String[] args) {
		try{			
			Outline outline = new Outline();			
		}catch(Exception e){
			System.out.println(e);
		}
		
	}

}