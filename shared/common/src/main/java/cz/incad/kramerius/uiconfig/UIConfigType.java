package cz.incad.kramerius.uiconfig;

public enum UIConfigType {
    GENERAL("general.json"),
    LICENSES("licenses.json"),
    CURATOR_LISTS("curator-lists.json");

    private final String fileName;

    UIConfigType(String fileName) {
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }
}