package cz.inovatika.kramerius.fedora.impl;

public class SupportedFormats {
    private final boolean supportsString;
    private final boolean supportsStream;
    private final boolean supportsXml;

    public SupportedFormats(boolean supportsString, boolean supportsStream, boolean supportsXml) {
        this.supportsString = supportsString;
        this.supportsStream = supportsStream;
        this.supportsXml = supportsXml;
    }

    public boolean supportsString() {
        return supportsString;
    }

    public boolean supportsStream() {
        return supportsStream;
    }

    public boolean supportsXml() {
        return supportsXml;
    }
}