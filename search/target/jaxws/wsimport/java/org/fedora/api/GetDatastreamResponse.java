
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
 *         &lt;element name="datastream" type="{http://www.fedora.info/definitions/1/0/types/}Datastream"/>
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
    "datastream"
})
@XmlRootElement(name = "getDatastreamResponse")
public class GetDatastreamResponse {

    @XmlElement(required = true)
    protected Datastream datastream;

    /**
     * Gets the value of the datastream property.
     * 
     * @return
     *     possible object is
     *     {@link Datastream }
     *     
     */
    public Datastream getDatastream() {
        return datastream;
    }

    /**
     * Sets the value of the datastream property.
     * 
     * @param value
     *     allowed object is
     *     {@link Datastream }
     *     
     */
    public void setDatastream(Datastream value) {
        this.datastream = value;
    }

}
