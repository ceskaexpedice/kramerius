
package org.fedora.api;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.fedora.api package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ListSessionExpirationDate_QNAME = new QName("", "expirationDate");
    private final static QName _ObjectFieldsCDate_QNAME = new QName("", "cDate");
    private final static QName _ObjectFieldsOwnerId_QNAME = new QName("", "ownerId");
    private final static QName _ObjectFieldsState_QNAME = new QName("", "state");
    private final static QName _ObjectFieldsLabel_QNAME = new QName("", "label");
    private final static QName _ObjectFieldsPid_QNAME = new QName("", "pid");
    private final static QName _ObjectFieldsDcmDate_QNAME = new QName("", "dcmDate");
    private final static QName _ObjectFieldsMDate_QNAME = new QName("", "mDate");
    private final static QName _FieldSearchResultListSession_QNAME = new QName("", "listSession");
    private final static QName _FieldSearchQueryConditions_QNAME = new QName("", "conditions");
    private final static QName _FieldSearchQueryTerms_QNAME = new QName("", "terms");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.fedora.api
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetDatastreams }
     * 
     */
    public GetDatastreams createGetDatastreams() {
        return new GetDatastreams();
    }

    /**
     * Create an instance of {@link GetDatastreamDisseminationResponse }
     * 
     */
    public GetDatastreamDisseminationResponse createGetDatastreamDisseminationResponse() {
        return new GetDatastreamDisseminationResponse();
    }

    /**
     * Create an instance of {@link MethodParmDef }
     * 
     */
    public MethodParmDef createMethodParmDef() {
        return new MethodParmDef();
    }

    /**
     * Create an instance of {@link ModifyDatastreamByValueResponse }
     * 
     */
    public ModifyDatastreamByValueResponse createModifyDatastreamByValueResponse() {
        return new ModifyDatastreamByValueResponse();
    }

    /**
     * Create an instance of {@link ListDatastreams }
     * 
     */
    public ListDatastreams createListDatastreams() {
        return new ListDatastreams();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link GetObjectXMLResponse }
     * 
     */
    public GetObjectXMLResponse createGetObjectXMLResponse() {
        return new GetObjectXMLResponse();
    }

    /**
     * Create an instance of {@link GetNextPIDResponse }
     * 
     */
    public GetNextPIDResponse createGetNextPIDResponse() {
        return new GetNextPIDResponse();
    }

    /**
     * Create an instance of {@link GetObjectProfileResponse }
     * 
     */
    public GetObjectProfileResponse createGetObjectProfileResponse() {
        return new GetObjectProfileResponse();
    }

    /**
     * Create an instance of {@link IngestResponse }
     * 
     */
    public IngestResponse createIngestResponse() {
        return new IngestResponse();
    }

    /**
     * Create an instance of {@link GetDatastream }
     * 
     */
    public GetDatastream createGetDatastream() {
        return new GetDatastream();
    }

    /**
     * Create an instance of {@link DatastreamDef }
     * 
     */
    public DatastreamDef createDatastreamDef() {
        return new DatastreamDef();
    }

    /**
     * Create an instance of {@link GetDatastreamsResponse }
     * 
     */
    public GetDatastreamsResponse createGetDatastreamsResponse() {
        return new GetDatastreamsResponse();
    }

    /**
     * Create an instance of {@link PurgeRelationshipResponse }
     * 
     */
    public PurgeRelationshipResponse createPurgeRelationshipResponse() {
        return new PurgeRelationshipResponse();
    }

    /**
     * Create an instance of {@link ListSession }
     * 
     */
    public ListSession createListSession() {
        return new ListSession();
    }

    /**
     * Create an instance of {@link SetDatastreamStateResponse }
     * 
     */
    public SetDatastreamStateResponse createSetDatastreamStateResponse() {
        return new SetDatastreamStateResponse();
    }

    /**
     * Create an instance of {@link GetDisseminationResponse }
     * 
     */
    public GetDisseminationResponse createGetDisseminationResponse() {
        return new GetDisseminationResponse();
    }

    /**
     * Create an instance of {@link ListMethods }
     * 
     */
    public ListMethods createListMethods() {
        return new ListMethods();
    }

    /**
     * Create an instance of {@link PurgeObjectResponse }
     * 
     */
    public PurgeObjectResponse createPurgeObjectResponse() {
        return new PurgeObjectResponse();
    }

    /**
     * Create an instance of {@link FindObjectsResponse }
     * 
     */
    public FindObjectsResponse createFindObjectsResponse() {
        return new FindObjectsResponse();
    }

    /**
     * Create an instance of {@link ModifyDatastreamByReferenceResponse }
     * 
     */
    public ModifyDatastreamByReferenceResponse createModifyDatastreamByReferenceResponse() {
        return new ModifyDatastreamByReferenceResponse();
    }

    /**
     * Create an instance of {@link FieldSearchQuery }
     * 
     */
    public FieldSearchQuery createFieldSearchQuery() {
        return new FieldSearchQuery();
    }

    /**
     * Create an instance of {@link ModifyObject }
     * 
     */
    public ModifyObject createModifyObject() {
        return new ModifyObject();
    }

    /**
     * Create an instance of {@link DatastreamBindingMap.DsBindings }
     * 
     */
    public DatastreamBindingMap.DsBindings createDatastreamBindingMapDsBindings() {
        return new DatastreamBindingMap.DsBindings();
    }

    /**
     * Create an instance of {@link FieldSearchQuery.Conditions }
     * 
     */
    public FieldSearchQuery.Conditions createFieldSearchQueryConditions() {
        return new FieldSearchQuery.Conditions();
    }

    /**
     * Create an instance of {@link PurgeRelationship }
     * 
     */
    public PurgeRelationship createPurgeRelationship() {
        return new PurgeRelationship();
    }

    /**
     * Create an instance of {@link SetDatastreamVersionableResponse }
     * 
     */
    public SetDatastreamVersionableResponse createSetDatastreamVersionableResponse() {
        return new SetDatastreamVersionableResponse();
    }

    /**
     * Create an instance of {@link ObjectMethodsDef }
     * 
     */
    public ObjectMethodsDef createObjectMethodsDef() {
        return new ObjectMethodsDef();
    }

    /**
     * Create an instance of {@link ObjectMethodsDef.MethodParmDefs }
     * 
     */
    public ObjectMethodsDef.MethodParmDefs createObjectMethodsDefMethodParmDefs() {
        return new ObjectMethodsDef.MethodParmDefs();
    }

    /**
     * Create an instance of {@link AddRelationship }
     * 
     */
    public AddRelationship createAddRelationship() {
        return new AddRelationship();
    }

    /**
     * Create an instance of {@link AddDatastreamResponse }
     * 
     */
    public AddDatastreamResponse createAddDatastreamResponse() {
        return new AddDatastreamResponse();
    }

    /**
     * Create an instance of {@link ListMethodsResponse }
     * 
     */
    public ListMethodsResponse createListMethodsResponse() {
        return new ListMethodsResponse();
    }

    /**
     * Create an instance of {@link GetDatastreamResponse }
     * 
     */
    public GetDatastreamResponse createGetDatastreamResponse() {
        return new GetDatastreamResponse();
    }

    /**
     * Create an instance of {@link AddRelationshipResponse }
     * 
     */
    public AddRelationshipResponse createAddRelationshipResponse() {
        return new AddRelationshipResponse();
    }

    /**
     * Create an instance of {@link DatastreamBinding }
     * 
     */
    public DatastreamBinding createDatastreamBinding() {
        return new DatastreamBinding();
    }

    /**
     * Create an instance of {@link PurgeDatastream }
     * 
     */
    public PurgeDatastream createPurgeDatastream() {
        return new PurgeDatastream();
    }

    /**
     * Create an instance of {@link Datastream }
     * 
     */
    public Datastream createDatastream() {
        return new Datastream();
    }

    /**
     * Create an instance of {@link RelationshipTuple }
     * 
     */
    public RelationshipTuple createRelationshipTuple() {
        return new RelationshipTuple();
    }

    /**
     * Create an instance of {@link ArrayOfString }
     * 
     */
    public ArrayOfString createArrayOfString() {
        return new ArrayOfString();
    }

    /**
     * Create an instance of {@link MIMETypedStream }
     * 
     */
    public MIMETypedStream createMIMETypedStream() {
        return new MIMETypedStream();
    }

    /**
     * Create an instance of {@link PurgeObject }
     * 
     */
    public PurgeObject createPurgeObject() {
        return new PurgeObject();
    }

    /**
     * Create an instance of {@link SetDatastreamVersionable }
     * 
     */
    public SetDatastreamVersionable createSetDatastreamVersionable() {
        return new SetDatastreamVersionable();
    }

    /**
     * Create an instance of {@link FieldSearchResult.ResultList }
     * 
     */
    public FieldSearchResult.ResultList createFieldSearchResultResultList() {
        return new FieldSearchResult.ResultList();
    }

    /**
     * Create an instance of {@link ExportResponse }
     * 
     */
    public ExportResponse createExportResponse() {
        return new ExportResponse();
    }

    /**
     * Create an instance of {@link GetObjectProfile }
     * 
     */
    public GetObjectProfile createGetObjectProfile() {
        return new GetObjectProfile();
    }

    /**
     * Create an instance of {@link MIMETypedStream.Header }
     * 
     */
    public MIMETypedStream.Header createMIMETypedStreamHeader() {
        return new MIMETypedStream.Header();
    }

    /**
     * Create an instance of {@link ModifyDatastreamByValue }
     * 
     */
    public ModifyDatastreamByValue createModifyDatastreamByValue() {
        return new ModifyDatastreamByValue();
    }

    /**
     * Create an instance of {@link ObjectFields }
     * 
     */
    public ObjectFields createObjectFields() {
        return new ObjectFields();
    }

    /**
     * Create an instance of {@link Condition }
     * 
     */
    public Condition createCondition() {
        return new Condition();
    }

    /**
     * Create an instance of {@link GetObjectXML }
     * 
     */
    public GetObjectXML createGetObjectXML() {
        return new GetObjectXML();
    }

    /**
     * Create an instance of {@link AddDatastream }
     * 
     */
    public AddDatastream createAddDatastream() {
        return new AddDatastream();
    }

    /**
     * Create an instance of {@link RepositoryInfo }
     * 
     */
    public RepositoryInfo createRepositoryInfo() {
        return new RepositoryInfo();
    }

    /**
     * Create an instance of {@link SetDatastreamState }
     * 
     */
    public SetDatastreamState createSetDatastreamState() {
        return new SetDatastreamState();
    }

    /**
     * Create an instance of {@link DatastreamBindingMap }
     * 
     */
    public DatastreamBindingMap createDatastreamBindingMap() {
        return new DatastreamBindingMap();
    }

    /**
     * Create an instance of {@link ObjectProfile }
     * 
     */
    public ObjectProfile createObjectProfile() {
        return new ObjectProfile();
    }

    /**
     * Create an instance of {@link FindObjects }
     * 
     */
    public FindObjects createFindObjects() {
        return new FindObjects();
    }

    /**
     * Create an instance of {@link CompareDatastreamChecksumResponse }
     * 
     */
    public CompareDatastreamChecksumResponse createCompareDatastreamChecksumResponse() {
        return new CompareDatastreamChecksumResponse();
    }

    /**
     * Create an instance of {@link GetDissemination.Parameters }
     * 
     */
    public GetDissemination.Parameters createGetDisseminationParameters() {
        return new GetDissemination.Parameters();
    }

    /**
     * Create an instance of {@link Ingest }
     * 
     */
    public Ingest createIngest() {
        return new Ingest();
    }

    /**
     * Create an instance of {@link ModifyDatastreamByReference }
     * 
     */
    public ModifyDatastreamByReference createModifyDatastreamByReference() {
        return new ModifyDatastreamByReference();
    }

    /**
     * Create an instance of {@link DescribeRepository }
     * 
     */
    public DescribeRepository createDescribeRepository() {
        return new DescribeRepository();
    }

    /**
     * Create an instance of {@link ModifyObjectResponse }
     * 
     */
    public ModifyObjectResponse createModifyObjectResponse() {
        return new ModifyObjectResponse();
    }

    /**
     * Create an instance of {@link ListDatastreamsResponse }
     * 
     */
    public ListDatastreamsResponse createListDatastreamsResponse() {
        return new ListDatastreamsResponse();
    }

    /**
     * Create an instance of {@link ObjectProfile.ObjModels }
     * 
     */
    public ObjectProfile.ObjModels createObjectProfileObjModels() {
        return new ObjectProfile.ObjModels();
    }

    /**
     * Create an instance of {@link GetDatastreamHistory }
     * 
     */
    public GetDatastreamHistory createGetDatastreamHistory() {
        return new GetDatastreamHistory();
    }

    /**
     * Create an instance of {@link GetRelationshipsResponse }
     * 
     */
    public GetRelationshipsResponse createGetRelationshipsResponse() {
        return new GetRelationshipsResponse();
    }

    /**
     * Create an instance of {@link GetObjectHistory }
     * 
     */
    public GetObjectHistory createGetObjectHistory() {
        return new GetObjectHistory();
    }

    /**
     * Create an instance of {@link CompareDatastreamChecksum }
     * 
     */
    public CompareDatastreamChecksum createCompareDatastreamChecksum() {
        return new CompareDatastreamChecksum();
    }

    /**
     * Create an instance of {@link DescribeRepositoryResponse }
     * 
     */
    public DescribeRepositoryResponse createDescribeRepositoryResponse() {
        return new DescribeRepositoryResponse();
    }

    /**
     * Create an instance of {@link GetDatastreamHistoryResponse }
     * 
     */
    public GetDatastreamHistoryResponse createGetDatastreamHistoryResponse() {
        return new GetDatastreamHistoryResponse();
    }

    /**
     * Create an instance of {@link GetDatastreamDissemination }
     * 
     */
    public GetDatastreamDissemination createGetDatastreamDissemination() {
        return new GetDatastreamDissemination();
    }

    /**
     * Create an instance of {@link ResumeFindObjects }
     * 
     */
    public ResumeFindObjects createResumeFindObjects() {
        return new ResumeFindObjects();
    }

    /**
     * Create an instance of {@link PurgeDatastreamResponse }
     * 
     */
    public PurgeDatastreamResponse createPurgeDatastreamResponse() {
        return new PurgeDatastreamResponse();
    }

    /**
     * Create an instance of {@link GetDissemination }
     * 
     */
    public GetDissemination createGetDissemination() {
        return new GetDissemination();
    }

    /**
     * Create an instance of {@link GetObjectHistoryResponse }
     * 
     */
    public GetObjectHistoryResponse createGetObjectHistoryResponse() {
        return new GetObjectHistoryResponse();
    }

    /**
     * Create an instance of {@link Export }
     * 
     */
    public Export createExport() {
        return new Export();
    }

    /**
     * Create an instance of {@link FieldSearchResult }
     * 
     */
    public FieldSearchResult createFieldSearchResult() {
        return new FieldSearchResult();
    }

    /**
     * Create an instance of {@link GetRelationships }
     * 
     */
    public GetRelationships createGetRelationships() {
        return new GetRelationships();
    }

    /**
     * Create an instance of {@link GetNextPID }
     * 
     */
    public GetNextPID createGetNextPID() {
        return new GetNextPID();
    }

    /**
     * Create an instance of {@link ResumeFindObjectsResponse }
     * 
     */
    public ResumeFindObjectsResponse createResumeFindObjectsResponse() {
        return new ResumeFindObjectsResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "expirationDate", scope = ListSession.class)
    public JAXBElement<String> createListSessionExpirationDate(String value) {
        return new JAXBElement<String>(_ListSessionExpirationDate_QNAME, String.class, ListSession.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "cDate", scope = ObjectFields.class)
    public JAXBElement<String> createObjectFieldsCDate(String value) {
        return new JAXBElement<String>(_ObjectFieldsCDate_QNAME, String.class, ObjectFields.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ownerId", scope = ObjectFields.class)
    public JAXBElement<String> createObjectFieldsOwnerId(String value) {
        return new JAXBElement<String>(_ObjectFieldsOwnerId_QNAME, String.class, ObjectFields.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "state", scope = ObjectFields.class)
    public JAXBElement<String> createObjectFieldsState(String value) {
        return new JAXBElement<String>(_ObjectFieldsState_QNAME, String.class, ObjectFields.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "label", scope = ObjectFields.class)
    public JAXBElement<String> createObjectFieldsLabel(String value) {
        return new JAXBElement<String>(_ObjectFieldsLabel_QNAME, String.class, ObjectFields.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "pid", scope = ObjectFields.class)
    public JAXBElement<String> createObjectFieldsPid(String value) {
        return new JAXBElement<String>(_ObjectFieldsPid_QNAME, String.class, ObjectFields.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "dcmDate", scope = ObjectFields.class)
    public JAXBElement<String> createObjectFieldsDcmDate(String value) {
        return new JAXBElement<String>(_ObjectFieldsDcmDate_QNAME, String.class, ObjectFields.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "mDate", scope = ObjectFields.class)
    public JAXBElement<String> createObjectFieldsMDate(String value) {
        return new JAXBElement<String>(_ObjectFieldsMDate_QNAME, String.class, ObjectFields.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListSession }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "listSession", scope = FieldSearchResult.class)
    public JAXBElement<ListSession> createFieldSearchResultListSession(ListSession value) {
        return new JAXBElement<ListSession>(_FieldSearchResultListSession_QNAME, ListSession.class, FieldSearchResult.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FieldSearchQuery.Conditions }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "conditions", scope = FieldSearchQuery.class)
    public JAXBElement<FieldSearchQuery.Conditions> createFieldSearchQueryConditions(FieldSearchQuery.Conditions value) {
        return new JAXBElement<FieldSearchQuery.Conditions>(_FieldSearchQueryConditions_QNAME, FieldSearchQuery.Conditions.class, FieldSearchQuery.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "terms", scope = FieldSearchQuery.class)
    public JAXBElement<String> createFieldSearchQueryTerms(String value) {
        return new JAXBElement<String>(_FieldSearchQueryTerms_QNAME, String.class, FieldSearchQuery.class, value);
    }

}
