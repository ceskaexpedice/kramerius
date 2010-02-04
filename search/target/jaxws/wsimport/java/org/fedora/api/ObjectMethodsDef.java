
package org.fedora.api;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ObjectMethodsDef complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ObjectMethodsDef">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="serviceDefinitionPID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="methodName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="methodParmDefs">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="methodParmDef" type="{http://www.fedora.info/definitions/1/0/types/}MethodParmDef" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="asOfDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectMethodsDef", propOrder = {
    "pid",
    "serviceDefinitionPID",
    "methodName",
    "methodParmDefs",
    "asOfDate"
})
public class ObjectMethodsDef {

    @XmlElement(name = "PID", required = true)
    protected String pid;
    @XmlElement(required = true)
    protected String serviceDefinitionPID;
    @XmlElement(required = true)
    protected String methodName;
    @XmlElement(required = true)
    protected ObjectMethodsDef.MethodParmDefs methodParmDefs;
    @XmlElement(required = true, nillable = true)
    protected String asOfDate;

    /**
     * Gets the value of the pid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPID() {
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
    public void setPID(String value) {
        this.pid = value;
    }

    /**
     * Gets the value of the serviceDefinitionPID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceDefinitionPID() {
        return serviceDefinitionPID;
    }

    /**
     * Sets the value of the serviceDefinitionPID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceDefinitionPID(String value) {
        this.serviceDefinitionPID = value;
    }

    /**
     * Gets the value of the methodName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the value of the methodName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethodName(String value) {
        this.methodName = value;
    }

    /**
     * Gets the value of the methodParmDefs property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectMethodsDef.MethodParmDefs }
     *     
     */
    public ObjectMethodsDef.MethodParmDefs getMethodParmDefs() {
        return methodParmDefs;
    }

    /**
     * Sets the value of the methodParmDefs property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectMethodsDef.MethodParmDefs }
     *     
     */
    public void setMethodParmDefs(ObjectMethodsDef.MethodParmDefs value) {
        this.methodParmDefs = value;
    }

    /**
     * Gets the value of the asOfDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAsOfDate() {
        return asOfDate;
    }

    /**
     * Sets the value of the asOfDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAsOfDate(String value) {
        this.asOfDate = value;
    }


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
     *         &lt;element name="methodParmDef" type="{http://www.fedora.info/definitions/1/0/types/}MethodParmDef" maxOccurs="unbounded" minOccurs="0"/>
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
        "methodParmDef"
    })
    public static class MethodParmDefs {

        protected List<MethodParmDef> methodParmDef;

        /**
         * Gets the value of the methodParmDef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the methodParmDef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMethodParmDef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MethodParmDef }
         * 
         * 
         */
        public List<MethodParmDef> getMethodParmDef() {
            if (methodParmDef == null) {
                methodParmDef = new ArrayList<MethodParmDef>();
            }
            return this.methodParmDef;
        }

    }

}
