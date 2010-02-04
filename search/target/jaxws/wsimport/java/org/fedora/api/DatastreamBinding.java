
package org.fedora.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * binding key to datastream association
 * 
 * <p>Java class for DatastreamBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DatastreamBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bindKeyName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bindLabel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="datastreamID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="seqNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DatastreamBinding", propOrder = {
    "bindKeyName",
    "bindLabel",
    "datastreamID",
    "seqNo"
})
public class DatastreamBinding {

    @XmlElement(required = true, nillable = true)
    protected String bindKeyName;
    @XmlElement(required = true, nillable = true)
    protected String bindLabel;
    @XmlElement(required = true, nillable = true)
    protected String datastreamID;
    @XmlElement(required = true, nillable = true)
    protected String seqNo;

    /**
     * Gets the value of the bindKeyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBindKeyName() {
        return bindKeyName;
    }

    /**
     * Sets the value of the bindKeyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBindKeyName(String value) {
        this.bindKeyName = value;
    }

    /**
     * Gets the value of the bindLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBindLabel() {
        return bindLabel;
    }

    /**
     * Sets the value of the bindLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBindLabel(String value) {
        this.bindLabel = value;
    }

    /**
     * Gets the value of the datastreamID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatastreamID() {
        return datastreamID;
    }

    /**
     * Sets the value of the datastreamID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatastreamID(String value) {
        this.datastreamID = value;
    }

    /**
     * Gets the value of the seqNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeqNo() {
        return seqNo;
    }

    /**
     * Sets the value of the seqNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeqNo(String value) {
        this.seqNo = value;
    }

}
