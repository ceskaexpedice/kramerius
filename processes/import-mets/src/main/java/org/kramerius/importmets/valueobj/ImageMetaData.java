package org.kramerius.importmets.valueobj;

/**
 * Administrativni data pro obrazky
 * 
 * @author xholcik
 */
public class ImageMetaData {

  private String urn;

  private String sici;

  private String scanningDevice;

  private String scanningParameters;

  private String otherImagingInformation;

  public String getScanningDevice() {
    return scanningDevice;
  }

  public void setScanningDevice(String scanningDevice) {
    this.scanningDevice = scanningDevice;
  }

  public String getScanningParameters() {
    return scanningParameters;
  }

  public void setScanningParameters(String scanningParameters) {
    this.scanningParameters = scanningParameters;
  }

  public String getOtherImagingInformation() {
    return otherImagingInformation;
  }

  public void setOtherImagingInformation(String otherImagingInformation) {
    this.otherImagingInformation = otherImagingInformation;
  }

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
  }

  public String getSici() {
    return sici;
  }

  public void setSici(String sici) {
    this.sici = sici;
  }

}
