
package org.fedora.api;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="objectMethod" type="{http://www.fedora.info/definitions/1/0/types/}ObjectMethodsDef" maxOccurs="unbounded" minOccurs="0"/>
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
    "objectMethod"
})
@XmlRootElement(name = "listMethodsResponse")
public class ListMethodsResponse {

    protected List<ObjectMethodsDef> objectMethod;

    /**
     * Gets the value of the objectMethod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectMethod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectMethod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ObjectMethodsDef }
     * 
     * 
     */
    public List<ObjectMethodsDef> getObjectMethod() {
        if (objectMethod == null) {
            objectMethod = new ArrayList<ObjectMethodsDef>();
        }
        return this.objectMethod;
    }

}
