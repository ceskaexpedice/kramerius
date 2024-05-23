package cz.incad.kramerius.statistics.accesslogs;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;


public class LogRecord {
    // flat structure
    private String id;
    private String pid;
    private Date date;

    private String ipAddress;
    private String user;
    private String requestedUrl;
    private String sessionToken;
    private String rootTitle;
    private String rootModel;
    private String rootPid;

    private String reportedAction;

    private String dbVersion;
    private String kramVersion;
    private String providedByLicense;

    private String evaluatedMap;
    private String userSessionAttributes;

    private String ownPidpath;
    private String ownModelPath;

    private String dateStr;
    private String dateRangeStart;
    private String dateRangeEnd;

    
    private Set<String> licenses = new LinkedHashSet<>();
    private Set<String> issueDates = new LinkedHashSet<>();
    private Set<String> langs = new LinkedHashSet<>();
    private Set<String> titles = new LinkedHashSet<>();
    private Set<String> solrDates = new LinkedHashSet<>();

    private Set<String> publishers = new LinkedHashSet<>();
    private Set<String> authors = new LinkedHashSet<>();

    private Set<String> pidsPaths = new LinkedHashSet<>();
    private Set<String> modelsPaths = new LinkedHashSet<>();

    private Set<String> isbns = new LinkedHashSet<>();
    private Set<String> issns = new LinkedHashSet<>();
    private Set<String> ccnbs = new LinkedHashSet<>();

    private Map<String, String> fieldsFromHttpRequestHeaders = new LinkedHashMap<>();

    private Set<LogRecordDetail> details = new LinkedHashSet<>();

    public String getId() {
        return id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRequestedUrl() {
        return requestedUrl;
    }

    public void setRequestedUrl(String requestedUrl) {
        this.requestedUrl = requestedUrl;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    public String getKramVersion() {
        return kramVersion;
    }

    public void setKramVersion(String kramVersion) {
        this.kramVersion = kramVersion;
    }

    public void addLicense(String license) {
        this.licenses.add(license);
    }

    public Set<String> getLicenses() {
        return licenses;
    }

    public void removeLicense(String license) {
        this.licenses.remove(license);
    }

    public void setLicenses(Set<String> licenses) {
        this.licenses = licenses;
    }

    public String getProvidedByLicense() {
        return providedByLicense;
    }

    public void setProvidedByLicense(String providedByLicense) {
        this.providedByLicense = providedByLicense;
    }

    public Set<String> getIssueDates() {
        return issueDates;
    }

    public void setIssueDates(Set<String> issueDates) {
        this.issueDates = issueDates;
    }

    public void addIssueDate(String iDate) {
        this.issueDates.add(iDate);
    }

    public void removeIssueDate(String iDate) {
        this.issueDates.remove(iDate);
    }

    public void addLang(String lang) {
        this.langs.add(lang);
    }

    public void removeLang(String lang) {
        this.langs.remove(lang);
    }

    public Set<String> getLangs() {
        return langs;
    }

    public void setLangs(Set<String> langs) {
        this.langs = langs;
    }

    public Set<String> getTitles() {
        return titles;
    }

    public void setTitles(Set<String> titles) {
        this.titles = titles;
    }

    public void addTitle(String title) {
        this.titles.add(title);
    }

    public void removeTitle(String title) {
        this.titles.remove(title);
    }

    public Set<String> getSolrDates() {
        return solrDates;
    }
    
    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }
    
    public String getDateStr() {
        return dateStr;
    }
    
    public void setDateRangeStart(String dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }
    
    public String getDateRangeStart() {
        return dateRangeStart;
    }
    
    public void setDateRangeEnd(String dateRangeEnd) {
        this.dateRangeEnd = dateRangeEnd;
    }
    
    public String getDateRangeEnd() {
        return dateRangeEnd;
    }
    

    public Set<String> getAuthors() {
        return authors;
    }

    public void addAuthor(String author) {
        this.authors.add(author);
    }

    public void removeAuthor(String author) {
        this.authors.remove(author);
    }

    public void setAuthors(Set<String> authors) {
        this.authors = authors;
    }

    public Set<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(Set<String> publishers) {
        this.publishers = publishers;
    }

    public void addPublisher(String publisher) {
        this.publishers.add(publisher);
    }

    public void removePublisher(String publisher) {
        this.publishers.remove(publisher);
    }

    public String getEvaluatedMap() {
        return evaluatedMap;
    }

    public void setEvaluatedMap(String evaluatedMap) {
        this.evaluatedMap = evaluatedMap;
    }

    public String getUserSessionAttributes() {
        return userSessionAttributes;
    }

    public void setUserSessionAttributes(String userSessionAttributes) {
        this.userSessionAttributes = userSessionAttributes;
    }

    public String getOwnPidpath() {
        return ownPidpath;
    }

    public void setOwnPidpath(String ownPidpath) {
        this.ownPidpath = ownPidpath;
    }

    public String getOwnModelPath() {
        return ownModelPath;
    }

    public void setOwnModelPath(String ownModelPath) {
        this.ownModelPath = ownModelPath;
    }

    public Set<String> getPidsPaths() {
        return pidsPaths;
    }

    public void setPidsPaths(Set<String> pidsPaths) {
        this.pidsPaths = pidsPaths;
    }

    public Set<String> getModelsPaths() {
        return modelsPaths;
    }

    public void setModelsPaths(Set<String> modelsPaths) {
        this.modelsPaths = modelsPaths;
    }

    public Set<String> getISBNs() {
        return isbns;
    }

    public void setISBNs(Set<String> isbns) {
        this.isbns = isbns;
    }

    public void addISBN(String isbn) {
        this.isbns.add(isbn);
    }

    public Set<String> getISSNs() {
        return issns;
    }

    public void setISSNs(Set<String> issns) {
        this.issns = issns;
    }

    public void addISSN(String issn) {
        this.issns.add(issn);
    }

    public void removeISSN(String issn) {
        this.issns.add(issn);
    }

    public Set<String> getCCNBs() {
        return ccnbs;
    }

    public void setCCNBs(Set<String> ccnbs) {
        this.ccnbs = ccnbs;
    }

    public void addCCNB(String ccnb) {
        this.ccnbs.add(ccnb);
    }

    public void remvoeCCNB(String ccnb) {
        this.ccnbs.remove(ccnb);
    }

    public String getReportedAction() {
        return reportedAction;
    }

    public void setReportedAction(String reportedAction) {
        this.reportedAction = reportedAction;
    }

    public void addDetail(LogRecordDetail detail) {
        this.details.add(detail);
    }

    public void removeDetail(LogRecordDetail detail) {
        this.details.remove(detail);
    }

    public String getRootTitle() {
        return rootTitle;
    }

    public void setRootTitle(String rootTitle) {
        this.rootTitle = rootTitle;
    }

    public String getRootModel() {
        return rootModel;
    }

    public void setRootModel(String rootModel) {
        this.rootModel = rootModel;
    }

    public String getRootPid() {
        return rootPid;
    }

    public void setRootPid(String rootPid) {
        this.rootPid = rootPid;
    }

    public void setFieldsFromHttpRequestHeaders(Map<String, String> fieldsFromHttpRequestHeaders) {
        this.fieldsFromHttpRequestHeaders = fieldsFromHttpRequestHeaders;
    }

    private LogRecord(String id, String pid, Date date) {
        super();
        this.id = id;
        this.pid = pid;
        this.date = date;
    }

    public static LogRecord buildRecord(String pid) {
        return new LogRecord(UUID.randomUUID().toString(), pid, new Date());
    }

    public Document toSolrBatch(DocumentBuilderFactory documentFactory) throws ParserConfigurationException {
        DocumentBuilder document = documentFactory.newDocumentBuilder();
        Document doc = document.newDocument();
        Element add = doc.createElement("add");
        Element docElm = doc.createElement("doc");
        doc.appendChild(add);
        add.appendChild(docElm);

        Element idField = doc.createElement("field");
        idField.setAttribute("name", "id");
        idField.setTextContent(this.id);
        docElm.appendChild(idField);

        Element pidField = doc.createElement("field");
        pidField.setAttribute("name", "pid");
        pidField.setTextContent(this.pid);
        docElm.appendChild(pidField);

        Element ipddress = doc.createElement("field");
        ipddress.setAttribute("name", "ip_address");
        ipddress.setTextContent(this.ipAddress);
        docElm.appendChild(ipddress);

        Element requestedUrl = doc.createElement("field");
        requestedUrl.setAttribute("name", "requested_url");
        requestedUrl.setTextContent(this.requestedUrl);
        docElm.appendChild(requestedUrl);
        
        if (this.dateStr != null) {
            Element dateStrField = doc.createElement("field");
            requestedUrl.setAttribute("name", "date.str");
            requestedUrl.setTextContent(this.dateStr);
            docElm.appendChild(dateStrField);
        }

        if (this.dateRangeStart != null) {
            Element dateStrField = doc.createElement("field");
            requestedUrl.setAttribute("name", "date_range_start.year");
            requestedUrl.setTextContent(this.dateRangeStart);
            docElm.appendChild(dateStrField);
        }
        
        if (this.dateRangeEnd != null) {
            Element dateStrField = doc.createElement("field");
            requestedUrl.setAttribute("name", "date_range_end.year");
            requestedUrl.setTextContent(this.dateRangeEnd);
            docElm.appendChild(dateStrField);
        }
        
        if (this.rootTitle != null) {
            Element rootTitleElm = doc.createElement("field");
            rootTitleElm.setAttribute("name", "root_title");
            rootTitleElm.setTextContent(this.rootTitle);
            docElm.appendChild(rootTitleElm);
        }

        if (this.user != null) {
            Element userField = doc.createElement("field");
            userField.setAttribute("name", "user");
            userField.setTextContent(this.user);
            docElm.appendChild(userField);
        }

        for (String pidPath : this.pidsPaths) {
            Set<String> allPids = new LinkedHashSet<>();
            Element pidPathElm = doc.createElement("field");
            pidPathElm.setAttribute("name", "pid_paths");
            pidPathElm.setTextContent(pidPath);
            docElm.appendChild(pidPathElm);

            String[] split = pidPath.split("/");
            for (String pid : split) {
                allPids.add(pid);
            }
            for (String p : allPids) {
                Element pElm = doc.createElement("field");
                pElm.setAttribute("name", "all_pids");
                pElm.setTextContent(p);
                docElm.appendChild(pElm);
            }
        }

        if (this.ownModelPath != null) {
            Set<String> allModels = new LinkedHashSet<>();
            Element oModelElm = doc.createElement("field");
            oModelElm.setAttribute("name", "own_model_path");
            oModelElm.setTextContent(this.ownModelPath);
            docElm.appendChild(oModelElm);

            String[] split = this.ownModelPath.split("/");
            for (String pid : split) {
                allModels.add(pid);
            }
            for (String m : allModels) {
                Element pElm = doc.createElement("field");
                pElm.setAttribute("name", "all_models");
                pElm.setTextContent(m);
                docElm.appendChild(pElm);
            }
        }

        if (this.ownPidpath != null) {
            Element oModelElm = doc.createElement("field");
            oModelElm.setAttribute("name", "own_pid_path");
            oModelElm.setTextContent(this.ownPidpath);
            docElm.appendChild(oModelElm);
        }

        for (LogRecordDetail detail : this.details) {
            Element dElm = doc.createElement("field");
            dElm.setAttribute("name", "pids_" + detail.getModel());
            dElm.setTextContent(detail.getPid());
            docElm.appendChild(dElm);

        }

        if (this.sessionToken != null) {
            Element sessionToken = doc.createElement("field");
            sessionToken.setAttribute("name", "session_token");
            sessionToken.setTextContent(this.sessionToken);
            docElm.appendChild(sessionToken);
        }

        if (this.evaluatedMap != null) {
            Element evaluatedMap = doc.createElement("field");
            evaluatedMap.setAttribute("name", "evaluated_map");
            evaluatedMap.setTextContent(this.evaluatedMap);
            docElm.appendChild(evaluatedMap);
        }

        if (this.userSessionAttributes != null) {
            Element userSessionAttributes = doc.createElement("field");
            userSessionAttributes.setAttribute("name", "user_session_attributes");
            userSessionAttributes.setTextContent(this.userSessionAttributes);
            docElm.appendChild(userSessionAttributes);
        }

        Element dbVersion = doc.createElement("field");
        dbVersion.setAttribute("name", "db_version");
        dbVersion.setTextContent(this.dbVersion);
        docElm.appendChild(dbVersion);

        for (String license : this.licenses) {
            Element licenses = doc.createElement("field");
            licenses.setAttribute("name", "licences");
            licenses.setTextContent(license);
            docElm.appendChild(licenses);
        }

        if (this.providedByLicense != null) {
            Element providedByLicense = doc.createElement("field");
            providedByLicense.setAttribute("name", "provided_by_license");
            providedByLicense.setTextContent(this.providedByLicense);
            docElm.appendChild(providedByLicense);
        }

        if (this.reportedAction != null) {
            Element reportedAction = doc.createElement("field");
            reportedAction.setAttribute("name", "reported_action");
            reportedAction.setTextContent(this.reportedAction);
            docElm.appendChild(reportedAction);
        }

        for (String issueDate : this.issueDates) {
            Element issueDateElm = doc.createElement("field");
            issueDateElm.setAttribute("name", "issue_dates");
            issueDateElm.setTextContent(issueDate);
            docElm.appendChild(issueDateElm);
        }

        for (String lang : this.langs) {
            Element langElm = doc.createElement("field");
            langElm.setAttribute("name", "langs");
            langElm.setTextContent(lang);
            docElm.appendChild(langElm);
        }

        for (String title : this.titles) {
            Element titleElm = doc.createElement("field");
            titleElm.setAttribute("name", "titles");
            titleElm.setTextContent(title);
            docElm.appendChild(titleElm);
        }

        for (String sDate : this.solrDates) {
            Element sDateElm = doc.createElement("field");
            sDateElm.setAttribute("name", "titles");
            sDateElm.setTextContent(sDate);
            docElm.appendChild(sDateElm);
        }

        for (String auth : this.authors) {
            Element sAuthElm = doc.createElement("field");
            sAuthElm.setAttribute("name", "authors");
            sAuthElm.setTextContent(auth);
            docElm.appendChild(sAuthElm);
        }

        for (String publisher : this.publishers) {
            Element pElm = doc.createElement("field");
            pElm.setAttribute("name", "publishers");
            pElm.setTextContent(publisher);
            docElm.appendChild(pElm);
        }

        for (String isbn : this.isbns) {
            Element pElm = doc.createElement("field");
            pElm.setAttribute("name", "id_isbn");
            pElm.setTextContent(isbn);
            docElm.appendChild(pElm);
        }

        for (String issn : this.issns) {
            Element pElm = doc.createElement("field");
            pElm.setAttribute("name", "id_issn");
            pElm.setTextContent(issn);
            docElm.appendChild(pElm);
        }
        for (String issn : this.ccnbs) {
            Element pElm = doc.createElement("field");
            pElm.setAttribute("name", "id_ccnb");
            pElm.setTextContent(issn);
            docElm.appendChild(pElm);
        }

        for (String headerName : fieldsFromHttpRequestHeaders.keySet()) {
            Element pElm = doc.createElement("field");
            pElm.setAttribute("name", "hrh_" + headerName.toLowerCase().replaceAll("-", "_"));
            pElm.setTextContent(fieldsFromHttpRequestHeaders.get(headerName));
            docElm.appendChild(pElm);
        }

        return doc;
    }
}
