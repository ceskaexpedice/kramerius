package cz.incad.kramerius.pdf.impl;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;

public interface OutputStreams {
	
	public OutputStream newOutputStream() throws IOException;

}
