
package org.fedora.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RepositoryInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RepositoryInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repositoryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="repositoryVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="repositoryBaseURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="repositoryPIDNamespace" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="defaultExportFormat" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OAINamespace" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="adminEmailList" type="{http://www.fedora.info/definitions/1/0/types/}ArrayOfString"/>
 *         &lt;element name="samplePID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sampleOAIIdentifier" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sampleSearchURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sampleAccessURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sampleOAIURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="retainPIDs" type="{http://www.fedora.info/definitions/1/0/types/}ArrayOfString"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryInfo", propOrder = {
    "repositoryName",
    "repositoryVersion",
    "repositoryBaseURL",
    "repositoryPIDNamespace",
    "defaultExportFormat",
    "oaiNamespace",
    "adminEmailList",
    "samplePID",
    "sampleOAIIdentifier",
    "sampleSearchURL",
    "sampleAccessURL",
    "sampleOAIURL",
    "retainPIDs"
})
public class RepositoryInfo {

    @XmlElement(required = true, nillable = true)
    protected String repositoryName;
    @XmlElement(required = true, nillable = true)
    protected String repositoryVersion;
    @XmlElement(required = true, nillable = true)
    protected String repositoryBaseURL;
    @XmlElement(required = true, nillable = true)
    protected String repositoryPIDNamespace;
    @XmlElement(required = true, nillable = true)
    protected String defaultExportFormat;
    @XmlElement(name = "OAINamespace", required = true, nillable = true)
    protected String oaiNamespace;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfString adminEmailList;
    @XmlElement(required = true, nillable = true)
    protected String samplePID;
    @XmlElement(required = true, nillable = true)
    protected String sampleOAIIdentifier;
    @XmlElement(required = true, nillable = true)
    protected String sampleSearchURL;
    @XmlElement(required = true, nillable = true)
    protected String sampleAccessURL;
    @XmlElement(required = true, nillable = true)
    protected String sampleOAIURL;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfString retainPIDs;

    /**
     * Gets the value of the repositoryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Sets the value of the repositoryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryName(String value) {
        this.repositoryName = value;
    }

    /**
     * Gets the value of the repositoryVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryVersion() {
        return repositoryVersion;
    }

    /**
     * Sets the value of the repositoryVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryVersion(String value) {
        this.repositoryVersion = value;
    }

    /**
     * Gets the value of the repositoryBaseURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryBaseURL() {
        return repositoryBaseURL;
    }

    /**
     * Sets the value of the repositoryBaseURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryBaseURL(String value) {
        this.repositoryBaseURL = value;
    }

    /**
     * Gets the value of the repositoryPIDNamespace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRepositoryPIDNamespace() {
        return repositoryPIDNamespace;
    }

    /**
     * Sets the value of the repositoryPIDNamespace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRepositoryPIDNamespace(String value) {
        this.repositoryPIDNamespace = value;
    }

    /**
     * Gets the value of the defaultExportFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultExportFormat() {
        return defaultExportFormat;
    }

    /**
     * Sets the value of the defaultExportFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultExportFormat(String value) {
        this.defaultExportFormat = value;
    }

    /**
     * Gets the value of the oaiNamespace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOAINamespace() {
        return oaiNamespace;
    }

    /**
     * Sets the value of the oaiNamespace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOAINamespace(String value) {
        this.oaiNamespace = value;
    }

    /**
     * Gets the value of the adminEmailList property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getAdminEmailList() {
        return adminEmailList;
    }

    /**
     * Sets the value of the adminEmailList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setAdminEmailList(ArrayOfString value) {
        this.adminEmailList = value;
    }

    /**
     * Gets the value of the samplePID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSamplePID() {
        return samplePID;
    }

    /**
     * Sets the value of the samplePID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSamplePID(String value) {
        this.samplePID = value;
    }

    /**
     * Gets the value of the sampleOAIIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSampleOAIIdentifier() {
        return sampleOAIIdentifier;
    }

    /**
     * Sets the value of the sampleOAIIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSampleOAIIdentifier(String value) {
        this.sampleOAIIdentifier = value;
    }

    /**
     * Gets the value of the sampleSearchURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSampleSearchURL() {
        return sampleSearchURL;
    }

    /**
     * Sets the value of the sampleSearchURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSampleSearchURL(String value) {
        this.sampleSearchURL = value;
    }

    /**
     * Gets the value of the sampleAccessURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSampleAccessURL() {
        return sampleAccessURL;
    }

    /**
     * Sets the value of the sampleAccessURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSampleAccessURL(String value) {
        this.sampleAccessURL = value;
    }

    /**
     * Gets the value of the sampleOAIURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSampleOAIURL() {
        return sampleOAIURL;
    }

    /**
     * Sets the value of the sampleOAIURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSampleOAIURL(String value) {
        this.sampleOAIURL = value;
    }

    /**
     * Gets the value of the retainPIDs property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getRetainPIDs() {
        return retainPIDs;
    }

    /**
     * Sets the value of the retainPIDs property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setRetainPIDs(ArrayOfString value) {
        this.retainPIDs = value;
    }

}
