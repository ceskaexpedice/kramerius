package cz.incad.kramerius.utils.imgs;

public enum ImageMimeType {

	JPEG("image/jpeg","jpg", true, false),
	    //image/tiff
	TIFF("image/tiff","tiff", true, false),
	PNG("image/png","png", true, false),
	JPEG2000("image/jp2","jp2",true,false),
	
	XDJVU("image/x.djvu","djvu", false, true),
	VNDDJVU("image/vnd.djvu","djvu", false, true),
	DJVU("image/djvu","djvu", false, true),
	PDF("application/pdf","pdf",false, true);
	
	
	
	private String value;
	private boolean supportedbyJava;
	private boolean multipageFormat;
	private String defaultFileExtension;
	
	private ImageMimeType(String value, String defaultExtension, boolean javasupport, boolean multipageformat) {
		this.value = value;
		this.supportedbyJava = javasupport;
		this.multipageFormat = multipageformat;
		this.defaultFileExtension = defaultExtension;
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

	
	public String getDefaultFileExtension() {
        return defaultFileExtension;
    }

    public static ImageMimeType loadFromMimeType(String mime) {
		ImageMimeType[] values = values();
		for (ImageMimeType iType : values) {
			if (iType.getValue().equals(mime)) return iType;
		}
		return null;
	}
}
