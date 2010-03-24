package cz.incad.kramerius.pdf.model;

import com.qbizm.kramerius.ext.ontheflypdf.edi.Element;
import com.qbizm.kramerius.ext.ontheflypdf.edi.TextBlock;

public class OutlinedTextBlock extends Element implements Outlineable<TextBlock>{

	private String dest;
	private TextBlock block;
	
	public OutlinedTextBlock(String dest, TextBlock block) {
		super();
		this.dest = dest;
		this.block = block;
	}
	@Override
	public String getDest() {
		return this.dest;
	}
	@Override
	public TextBlock getWrapped() {
		return this.block;
	}
	
}
