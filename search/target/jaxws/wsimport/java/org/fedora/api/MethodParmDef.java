
package org.fedora.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MethodParmDef complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MethodParmDef">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parmName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parmType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parmDefaultValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parmDomainValues" type="{http://www.fedora.info/definitions/1/0/types/}ArrayOfString"/>
 *         &lt;element name="parmRequired" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="parmLabel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parmPassBy" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PASS_BY_REF" type="{http://www.fedora.info/definitions/1/0/types/}passByRef"/>
 *         &lt;element name="PASS_BY_VALUE" type="{http://www.fedora.info/definitions/1/0/types/}passByValue"/>
 *         &lt;element name="DATASTREAM_INPUT" type="{http://www.fedora.info/definitions/1/0/types/}datastreamInputType"/>
 *         &lt;element name="USER_INPUT" type="{http://www.fedora.info/definitions/1/0/types/}userInputType"/>
 *         &lt;element name="DEFAULT_INPUT" type="{http://www.fedora.info/definitions/1/0/types/}defaultInputType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MethodParmDef", propOrder = {
    "parmName",
    "parmType",
    "parmDefaultValue",
    "parmDomainValues",
    "parmRequired",
    "parmLabel",
    "parmPassBy",
    "passbyref",
    "passbyvalue",
    "datastreaminput",
    "userinput",
    "defaultinput"
})
public class MethodParmDef {

    @XmlElement(required = true, nillable = true)
    protected String parmName;
    @XmlElement(required = true, nillable = true)
    protected String parmType;
    @XmlElement(required = true, nillable = true)
    protected String parmDefaultValue;
    @XmlElement(required = true, nillable = true)
    protected ArrayOfString parmDomainValues;
    protected boolean parmRequired;
    @XmlElement(required = true, nillable = true)
    protected String parmLabel;
    @XmlElement(required = true, nillable = true)
    protected String parmPassBy;
    @XmlElement(name = "PASS_BY_REF", required = true, nillable = true)
    protected PassByRef passbyref;
    @XmlElement(name = "PASS_BY_VALUE", required = true, nillable = true)
    protected PassByValue passbyvalue;
    @XmlElement(name = "DATASTREAM_INPUT", required = true, nillable = true)
    protected DatastreamInputType datastreaminput;
    @XmlElement(name = "USER_INPUT", required = true, nillable = true)
    protected UserInputType userinput;
    @XmlElement(name = "DEFAULT_INPUT", required = true, nillable = true)
    protected DefaultInputType defaultinput;

    /**
     * Gets the value of the parmName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParmName() {
        return parmName;
    }

    /**
     * Sets the value of the parmName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParmName(String value) {
        this.parmName = value;
    }

    /**
     * Gets the value of the parmType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParmType() {
        return parmType;
    }

    /**
     * Sets the value of the parmType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParmType(String value) {
        this.parmType = value;
    }

    /**
     * Gets the value of the parmDefaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParmDefaultValue() {
        return parmDefaultValue;
    }

    /**
     * Sets the value of the parmDefaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParmDefaultValue(String value) {
        this.parmDefaultValue = value;
    }

    /**
     * Gets the value of the parmDomainValues property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getParmDomainValues() {
        return parmDomainValues;
    }

    /**
     * Sets the value of the parmDomainValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setParmDomainValues(ArrayOfString value) {
        this.parmDomainValues = value;
    }

    /**
     * Gets the value of the parmRequired property.
     * 
     */
    public boolean isParmRequired() {
        return parmRequired;
    }

    /**
     * Sets the value of the parmRequired property.
     * 
     */
    public void setParmRequired(boolean value) {
        this.parmRequired = value;
    }

    /**
     * Gets the value of the parmLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParmLabel() {
        return parmLabel;
    }

    /**
     * Sets the value of the parmLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParmLabel(String value) {
        this.parmLabel = value;
    }

    /**
     * Gets the value of the parmPassBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParmPassBy() {
        return parmPassBy;
    }

    /**
     * Sets the value of the parmPassBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParmPassBy(String value) {
        this.parmPassBy = value;
    }

    /**
     * Gets the value of the passbyref property.
     * 
     * @return
     *     possible object is
     *     {@link PassByRef }
     *     
     */
    public PassByRef getPASSBYREF() {
        return passbyref;
    }

    /**
     * Sets the value of the passbyref property.
     * 
     * @param value
     *     allowed object is
     *     {@link PassByRef }
     *     
     */
    public void setPASSBYREF(PassByRef value) {
        this.passbyref = value;
    }

    /**
     * Gets the value of the passbyvalue property.
     * 
     * @return
     *     possible object is
     *     {@link PassByValue }
     *     
     */
    public PassByValue getPASSBYVALUE() {
        return passbyvalue;
    }

    /**
     * Sets the value of the passbyvalue property.
     * 
     * @param value
     *     allowed object is
     *     {@link PassByValue }
     *     
     */
    public void setPASSBYVALUE(PassByValue value) {
        this.passbyvalue = value;
    }

    /**
     * Gets the value of the datastreaminput property.
     * 
     * @return
     *     possible object is
     *     {@link DatastreamInputType }
     *     
     */
    public DatastreamInputType getDATASTREAMINPUT() {
        return datastreaminput;
    }

    /**
     * Sets the value of the datastreaminput property.
     * 
     * @param value
     *     allowed object is
     *     {@link DatastreamInputType }
     *     
     */
    public void setDATASTREAMINPUT(DatastreamInputType value) {
        this.datastreaminput = value;
    }

    /**
     * Gets the value of the userinput property.
     * 
     * @return
     *     possible object is
     *     {@link UserInputType }
     *     
     */
    public UserInputType getUSERINPUT() {
        return userinput;
    }

    /**
     * Sets the value of the userinput property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInputType }
     *     
     */
    public void setUSERINPUT(UserInputType value) {
        this.userinput = value;
    }

    /**
     * Gets the value of the defaultinput property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultInputType }
     *     
     */
    public DefaultInputType getDEFAULTINPUT() {
        return defaultinput;
    }

    /**
     * Sets the value of the defaultinput property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultInputType }
     *     
     */
    public void setDEFAULTINPUT(DefaultInputType value) {
        this.defaultinput = value;
    }

}
