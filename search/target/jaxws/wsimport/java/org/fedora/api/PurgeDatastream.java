
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
 *         &lt;element name="startDT" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="endDT" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="force" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "startDT",
    "endDT",
    "logMessage",
    "force"
})
@XmlRootElement(name = "purgeDatastream")
public class PurgeDatastream {

    @XmlElement(required = true)
    protected String pid;
    @XmlElement(required = true)
    protected String dsID;
    @XmlElement(required = true, nillable = true)
    protected String startDT;
    @XmlElement(required = true, nillable = true)
    protected String endDT;
    @XmlElement(required = true, nillable = true)
    protected String logMessage;
    protected boolean force;

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
     * Gets the value of the startDT property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartDT() {
        return startDT;
    }

    /**
     * Sets the value of the startDT property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartDT(String value) {
        this.startDT = value;
    }

    /**
     * Gets the value of the endDT property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndDT() {
        return endDT;
    }

    /**
     * Sets the value of the endDT property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndDT(String value) {
        this.endDT = value;
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

    /**
     * Gets the value of the force property.
     * 
     */
    public boolean isForce() {
        return force;
    }

    /**
     * Sets the value of the force property.
     * 
     */
    public void setForce(boolean value) {
        this.force = value;
    }

}
