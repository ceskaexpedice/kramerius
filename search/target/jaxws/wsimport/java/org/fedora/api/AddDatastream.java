
package org.fedora.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dsID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="altIDs" type="{http://www.fedora.info/definitions/1/0/types/}ArrayOfString"/>
 *         &lt;element name="dsLabel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="versionable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="MIMEType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="formatURI" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dsLocation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="controlGroup" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dsState" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="checksumType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="checksum" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pid",
    "dsID",
    "altIDs",
    "dsLabel",
    "versionable",
    "mimeType",
    "formatURI",
    "dsLocation",
    "controlGroup",
    "dsState",
    "checksumType",
    "checksum",
    "logMessage"
})
@XmlRootElement(name = "addDatastream")
public class AddDatastream {

    @XmlElement(required = true)
    protected String pid;
    @XmlElement(required = true, nillable = true)
    protected String dsID;
    @XmlElement(required = true)
    protected ArrayOfString altIDs;
    @XmlElement(required = true)
    protected String dsLabel;
    protected boolean versionable;
    @XmlElement(name = "MIMEType", required = true)
    protected String mimeType;
    @XmlElement(required = true)
    protected String formatURI;
    @XmlElement(required = true)
    protected String dsLocation;
    @XmlElement(required = true)
    protected String controlGroup;
    @XmlElement(required = true)
    protected String dsState;
    @XmlElement(required = true)
    protected String checksumType;
    @XmlElement(required = true)
    protected String checksum;
    @XmlElement(required = true)
    protected String logMessage;

    /**
     * Gets the value of the pid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPid() {
        return pid;
    }

    /**
     * Sets the value of the pid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPid(String value) {
        this.pid = value;
    }

    /**
     * Gets the value of the dsID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDsID() {
        return dsID;
    }

    /**
     * Sets the value of the dsID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDsID(String value) {
        this.dsID = value;
    }

    /**
     * Gets the value of the altIDs property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getAltIDs() {
        return altIDs;
    }

    /**
     * Sets the value of the altIDs property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setAltIDs(ArrayOfString value) {
        this.altIDs = value;
    }

    /**
     * Gets the value of the dsLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDsLabel() {
        return dsLabel;
    }

    /**
     * Sets the value of the dsLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDsLabel(String value) {
        this.dsLabel = value;
    }

    /**
     * Gets the value of the versionable property.
     * 
     */
    public boolean isVersionable() {
        return versionable;
    }

    /**
     * Sets the value of the versionable property.
     * 
     */
    public void setVersionable(boolean value) {
        this.versionable = value;
    }

    /**
     * Gets the value of the mimeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMIMEType() {
        return mimeType;
    }

    /**
     * Sets the value of the mimeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMIMEType(String value) {
        this.mimeType = value;
    }

    /**
     * Gets the value of the formatURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatURI() {
        return formatURI;
    }

    /**
     * Sets the value of the formatURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatURI(String value) {
        this.formatURI = value;
    }

    /**
     * Gets the value of the dsLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDsLocation() {
        return dsLocation;
    }

    /**
     * Sets the value of the dsLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDsLocation(String value) {
        this.dsLocation = value;
    }

    /**
     * Gets the value of the controlGroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getControlGroup() {
        return controlGroup;
    }

    /**
     * Sets the value of the controlGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setControlGroup(String value) {
        this.controlGroup = value;
    }

    /**
     * Gets the value of the dsState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDsState() {
        return dsState;
    }

    /**
     * Sets the value of the dsState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDsState(String value) {
        this.dsState = value;
    }

    /**
     * Gets the value of the checksumType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecksumType() {
        return checksumType;
    }

    /**
     * Sets the value of the checksumType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecksumType(String value) {
        this.checksumType = value;
    }

    /**
     * Gets the value of the checksum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the value of the checksum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChecksum(String value) {
        this.checksum = value;
    }

    /**
     * Gets the value of the logMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogMessage() {
        return logMessage;
    }

    /**
     * Sets the value of the logMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogMessage(String value) {
        this.logMessage = value;
    }

}
