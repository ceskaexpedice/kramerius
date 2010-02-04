
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
 *         &lt;element name="dissemination" type="{http://www.fedora.info/definitions/1/0/types/}MIMETypedStream"/>
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
    "dissemination"
})
@XmlRootElement(name = "getDatastreamDisseminationResponse")
public class GetDatastreamDisseminationResponse {

    @XmlElement(required = true)
    protected MIMETypedStream dissemination;

    /**
     * Gets the value of the dissemination property.
     * 
     * @return
     *     possible object is
     *     {@link MIMETypedStream }
     *     
     */
    public MIMETypedStream getDissemination() {
        return dissemination;
    }

    /**
     * Sets the value of the dissemination property.
     * 
     * @param value
     *     allowed object is
     *     {@link MIMETypedStream }
     *     
     */
    public void setDissemination(MIMETypedStream value) {
        this.dissemination = value;
    }

}
