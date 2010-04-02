package cz.incad.kramerius.utils.imgs;

public enum ImageMimeType {

	JPEG("image/jpeg", true),
	PNG("image/png", true),
	XDJVU("image/x.djvu", false),
	DJVU("image/djvu", false);

	private String value;
	private boolean supportedbyJava;
	
	private ImageMimeType(String value, boolean javasupport) {
		this.value = value;
		this.supportedbyJava = javasupport;
	}

	public String getValue() {
		return value;
	}

	public boolean isSupportedbyJava() {
		return supportedbyJava;
	}
	
	public static ImageMimeType loadFromMimeType(String mime) {
		ImageMimeType[] values = values();
		for (ImageMimeType iType : values) {
			if (iType.getValue().equals(mime)) return iType;
		}
		return null;
	}
}
