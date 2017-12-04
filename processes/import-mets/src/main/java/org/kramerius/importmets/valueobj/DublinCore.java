package org.kramerius.importmets.valueobj;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object pro Dublin core stream
 * 
 * @author xholcik
 */
public class DublinCore {

    private String title;

    private List<String> identifier;

    private List<String> creator;

    private List<String> publisher;

    private List<String> contributor;

    private String date;

    private String language;

    private String description;

    private String format;

    private List<String> subject;

    private String type;

    private String rights;

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

    public List<String> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<String> identifier) {
        this.identifier = identifier;
    }

    public void addIdentifier(String id) {
        if (id == null || "".equals(id))
            return;
        if (this.identifier == null) {
            this.identifier = new ArrayList<String>();
        }
        this.identifier.add(id);
    }
    
    public void addQualifiedIdentifier(String prefix, String id) {
        if (id == null || "".equals(id))
            return;
        if (this.identifier == null) {
            this.identifier = new ArrayList<String>();
        }
        this.identifier.add(prefix+":"+id);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getSubject() {
        return subject;
    }

    public void setSubject(List<String> subject) {
        this.subject = subject;
    }
    
    public void addSubject(String subj) {
        if (subj == null || "".equals(subj))
            return;
        if (this.subject == null) {
            this.subject = new ArrayList<String>();
        }
        this.subject.add(subj);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

}
