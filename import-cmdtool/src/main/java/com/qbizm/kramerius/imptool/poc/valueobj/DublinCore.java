package com.qbizm.kramerius.imptool.poc.valueobj;

import java.util.List;

/**
 * Value object pro Dublin core stream
 * 
 * @author xholcik
 */
public class DublinCore {

  private String title;

  private List<String> creator;

  private List<String> publisher;

  private List<String> contributor;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  
  public List<String> getCreator() {
    return creator;
  }

  
  public void setCreator(List<String> creator) {
    this.creator = creator;
  }

  
  public List<String> getPublisher() {
    return publisher;
  }

  
  public void setPublisher(List<String> publisher) {
    this.publisher = publisher;
  }

  
  public List<String> getContributor() {
    return contributor;
  }

  
  public void setContributor(List<String> contributor) {
    this.contributor = contributor;
  }


}
