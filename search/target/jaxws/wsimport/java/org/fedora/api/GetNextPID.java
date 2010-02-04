
package org.fedora.api;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element name="numPIDs" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="pidNamespace" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "numPIDs",
    "pidNamespace"
})
@XmlRootElement(name = "getNextPID")
public class GetNextPID {

    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger numPIDs;
    @XmlElement(required = true, nillable = true)
    protected String pidNamespace;

    /**
     * Gets the value of the numPIDs property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumPIDs() {
        return numPIDs;
    }

    /**
     * Sets the value of the numPIDs property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumPIDs(BigInteger value) {
        this.numPIDs = value;
    }

    /**
     * Gets the value of the pidNamespace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPidNamespace() {
        return pidNamespace;
    }

    /**
     * Sets the value of the pidNamespace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPidNamespace(String value) {
        this.pidNamespace = value;
    }

}
