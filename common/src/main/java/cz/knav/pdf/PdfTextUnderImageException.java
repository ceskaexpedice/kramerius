package cz.knav.pdf;

public class PdfTextUnderImageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PdfTextUnderImageException() {
		super();
	}

	public PdfTextUnderImageException(String s) {
		super(s);
	}
	
	public PdfTextUnderImageException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}