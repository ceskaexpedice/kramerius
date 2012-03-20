package org.kramerius.importmets.valueobj;

/**
 * Reprezentace digitalniho objektu
 *
 * @author xholcik
 */
public class ImageRepresentation {

    private String filename;

    private StreamFileType fileType;



    public ImageRepresentation(String filename, StreamFileType fileType) {
        super();
        this.filename = filename;
        this.fileType = fileType;
    }

    public StreamFileType getFileType() {
        return fileType;
    }

    public void setFileType(StreamFileType fileType) {
        this.fileType = fileType;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


}
