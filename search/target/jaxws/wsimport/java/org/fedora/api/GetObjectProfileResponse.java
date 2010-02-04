
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
 *         &lt;element name="objectProfile" type="{http://www.fedora.info/definitions/1/0/types/}ObjectProfile"/>
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
    "objectProfile"
})
@XmlRootElement(name = "getObjectProfileResponse")
public class GetObjectProfileResponse {

    @XmlElement(required = true)
    protected ObjectProfile objectProfile;

    /**
     * Gets the value of the objectProfile property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectProfile }
     *     
     */
    public ObjectProfile getObjectProfile() {
        return objectProfile;
    }

    /**
     * Sets the value of the objectProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectProfile }
     *     
     */
    public void setObjectProfile(ObjectProfile value) {
        this.objectProfile = value;
    }

}
