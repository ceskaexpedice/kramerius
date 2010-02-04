
package org.fedora.api;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FieldSearchQuery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FieldSearchQuery">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="conditions">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="condition" type="{http://www.fedora.info/definitions/1/0/types/}Condition" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="terms" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FieldSearchQuery", propOrder = {
    "conditions",
    "terms"
})
public class FieldSearchQuery {

    @XmlElementRef(name = "conditions", type = JAXBElement.class)
    protected JAXBElement<FieldSearchQuery.Conditions> conditions;
    @XmlElementRef(name = "terms", type = JAXBElement.class)
    protected JAXBElement<String> terms;

    /**
     * Gets the value of the conditions property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link FieldSearchQuery.Conditions }{@code >}
     *     
     */
    public JAXBElement<FieldSearchQuery.Conditions> getConditions() {
        return conditions;
    }

    /**
     * Sets the value of the conditions property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link FieldSearchQuery.Conditions }{@code >}
     *     
     */
    public void setConditions(JAXBElement<FieldSearchQuery.Conditions> value) {
        this.conditions = ((JAXBElement<FieldSearchQuery.Conditions> ) value);
    }

    /**
     * Gets the value of the terms property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTerms() {
        return terms;
    }

    /**
     * Sets the value of the terms property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTerms(JAXBElement<String> value) {
        this.terms = ((JAXBElement<String> ) value);
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
     *         &lt;element name="condition" type="{http://www.fedora.info/definitions/1/0/types/}Condition" maxOccurs="unbounded" minOccurs="0"/>
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
        "condition"
    })
    public static class Conditions {

        protected List<Condition> condition;

        /**
         * Gets the value of the condition property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the condition property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCondition().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Condition }
         * 
         * 
         */
        public List<Condition> getCondition() {
            if (condition == null) {
                condition = new ArrayList<Condition>();
            }
            return this.condition;
        }

    }

}
