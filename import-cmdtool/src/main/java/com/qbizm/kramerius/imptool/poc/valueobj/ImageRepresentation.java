package com.qbizm.kramerius.imptool.poc.valueobj;

/**
 * Reprezentace digitalniho objektu
 * 
 * @author xholcik
 */
public class ImageRepresentation {

  private String filename;

  private ImageMetaData imageMetaData;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public ImageMetaData getImageMetaData() {
    return imageMetaData;
  }

  public void setImageMetaData(ImageMetaData imageMetaData) {
    this.imageMetaData = imageMetaData;
  }

}
