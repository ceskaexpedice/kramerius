
package org.fedora.api;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * ordered list of datastream binding maps
 * 
 * <p>Java class for DatastreamBindingMap complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DatastreamBindingMap">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dsBindMapID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dsBindMechanismPID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dsBindMapLabel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="state" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dsBindings">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="dsBinding" type="{http://www.fedora.info/definitions/1/0/types/}DatastreamBinding" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DatastreamBindingMap", propOrder = {
    "dsBindMapID",
    "dsBindMechanismPID",
    "dsBindMapLabel",
    "state",
    "dsBindings"
})
public class DatastreamBindingMap {

    @XmlElement(required = true, nillable = true)
    protected String dsBindMapID;
    @XmlElement(required = true, nillable = true)
    protected String dsBindMechanismPID;
    @XmlElement(required = true, nillable = true)
    protected String dsBindMapLabel;
    @XmlElement(required = true, nillable = true)
    protected String state;
    @XmlElement(required = true, nillable = true)
    protected DatastreamBindingMap.DsBindings dsBindings;

    /**
     * Gets the value of the dsBindMapID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDsBindMapID() {
        return dsBindMapID;
    }

    /**
     * Sets the value of the dsBindMapID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDsBindMapID(String value) {
        this.dsBindMapID = value;
    }

    /**
     * Gets the value of the dsBindMechanismPID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDsBindMechanismPID() {
        return dsBindMechanismPID;
    }

    /**
     * Sets the value of the dsBindMechanismPID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDsBindMechanismPID(String value) {
        this.dsBindMechanismPID = value;
    }

    /**
     * Gets the value of the dsBindMapLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDsBindMapLabel() {
        return dsBindMapLabel;
    }

    /**
     * Sets the value of the dsBindMapLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDsBindMapLabel(String value) {
        this.dsBindMapLabel = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(String value) {
        this.state = value;
    }

    /**
     * Gets the value of the dsBindings property.
     * 
     * @return
     *     possible object is
     *     {@link DatastreamBindingMap.DsBindings }
     *     
     */
    public DatastreamBindingMap.DsBindings getDsBindings() {
        return dsBindings;
    }

    /**
     * Sets the value of the dsBindings property.
     * 
     * @param value
     *     allowed object is
     *     {@link DatastreamBindingMap.DsBindings }
     *     
     */
    public void setDsBindings(DatastreamBindingMap.DsBindings value) {
        this.dsBindings = value;
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
     *         &lt;element name="dsBinding" type="{http://www.fedora.info/definitions/1/0/types/}DatastreamBinding" maxOccurs="unbounded" minOccurs="0"/>
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
        "dsBinding"
    })
    public static class DsBindings {

        protected List<DatastreamBinding> dsBinding;

        /**
         * Gets the value of the dsBinding property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the dsBinding property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDsBinding().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link DatastreamBinding }
         * 
         * 
         */
        public List<DatastreamBinding> getDsBinding() {
            if (dsBinding == null) {
                dsBinding = new ArrayList<DatastreamBinding>();
            }
            return this.dsBinding;
        }

    }

}
