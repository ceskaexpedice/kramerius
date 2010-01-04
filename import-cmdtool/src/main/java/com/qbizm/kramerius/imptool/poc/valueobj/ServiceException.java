package com.qbizm.kramerius.imptool.poc.valueobj;

/**
 * Standardni vyjimka 
 * @author xholcik
 */
public class ServiceException extends Exception {

  private static final long serialVersionUID = 1L;

  public ServiceException() {
    super();
  }

  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(Throwable cause) {
    super(cause);
  }

}
