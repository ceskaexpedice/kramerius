package cz.incad.kramerius.lp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import cz.incad.kramerius.lp.utils.ASCIITranslate;
import cz.incad.kramerius.pdf.Break;
import cz.incad.kramerius.pdf.impl.OutputStreams;

public class GenerateController implements Break, OutputStreams {

	public static final long ONE_FILE_LIMIT = 100l << 20;

	private File curFile;
	private DecoratedOutputStream currentDos;
	private int pocitadlo = 1;
	
	private File folder;
	private String name;
	private long velikost = ONE_FILE_LIMIT;
	
	public GenerateController(File folder, String name) {
		super();
		this.folder = folder;
		this.name = ASCIITranslate.asciiString(name.trim());
		this.corruptName();
	}


	public void corruptName() {
		StringBuffer buff = new StringBuffer();
		for (int i=0,ll=this.name.length();i<ll;i++) {
			char charAt = this.name.charAt(i);
			if (!Character.isWhitespace(charAt)) {
				buff.append(charAt);
			} else break;
		}
		if (this.name.equals("")) {
			this.name = "part_";
		} else {
			this.name = buff.toString();
		}
	}
	
	
	@Override
	public OutputStream newOutputStream() throws IOException {
		this.curFile = new File(this.folder,this.name+"_"+pocitadlo+".pdf");
		this.currentDos = new DecoratedOutputStream(this.curFile);
		this.pocitadlo += 1;
		return this.currentDos;
	}

	@Override
	public boolean broken(String uuid) {
		if (this.currentDos.getActualSize() >= this.velikost) return true;
		return false;
	}
	
}
