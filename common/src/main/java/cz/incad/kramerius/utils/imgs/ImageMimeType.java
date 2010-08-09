package cz.incad.kramerius.utils.imgs;

public enum ImageMimeType {

	JPEG("image/jpeg", true, false),
	PNG("image/png", true, false),

	XDJVU("image/x.djvu", false, true),
	VNDDJVU("image/vnd.djvu", false, true),
	DJVU("image/djvu", false, true),
	PDF("application/pdf",false, true);
	
	
	private String value;
	private boolean supportedbyJava;
	private boolean multipageFormat;
	
	private ImageMimeType(String value, boolean javasupport, boolean multipageformat) {
		this.value = value;
		this.supportedbyJava = javasupport;
		this.multipageFormat = multipageformat;
	}

	public String getValue() {
		return value;
	}

	public boolean javaNativeSupport() {
		return supportedbyJava;
	}
	
	public boolean isMultipageFormat()  {
		return this.multipageFormat;
	}
	
	
	public static ImageMimeType loadFromMimeType(String mime) {
		ImageMimeType[] values = values();
		for (ImageMimeType iType : values) {
			if (iType.getValue().equals(mime)) return iType;
		}
		return null;
	}
}
