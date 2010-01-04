package com.qbizm.kramerius.imptool.poc.valueobj;

import javax.xml.bind.Marshaller;

/**
 * Konfigurace konvertoru
 * 
 * @author xholcik
 */
public class ConvertorConfig {

  private Marshaller marshaller;

  private String importFolder;

  private String exportFolder;

  public String getImportFolder() {
    return importFolder;
  }

  public void setImportFolder(String importFolder) {
    this.importFolder = importFolder;
  }

  public String getExportFolder() {
    return exportFolder;
  }

  public void setExportFolder(String exportFolder) {
    this.exportFolder = exportFolder;
  }

  public Marshaller getMarshaller() {
    return marshaller;
  }

  public void setMarshaller(Marshaller marshaller) {
    this.marshaller = marshaller;
  }

}
