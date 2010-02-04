
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
 *         &lt;element name="repositoryInfo" type="{http://www.fedora.info/definitions/1/0/types/}RepositoryInfo"/>
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
    "repositoryInfo"
})
@XmlRootElement(name = "describeRepositoryResponse")
public class DescribeRepositoryResponse {

    @XmlElement(required = true)
    protected RepositoryInfo repositoryInfo;

    /**
     * Gets the value of the repositoryInfo property.
     * 
     * @return
     *     possible object is
     *     {@link RepositoryInfo }
     *     
     */
    public RepositoryInfo getRepositoryInfo() {
        return repositoryInfo;
    }

    /**
     * Sets the value of the repositoryInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link RepositoryInfo }
     *     
     */
    public void setRepositoryInfo(RepositoryInfo value) {
        this.repositoryInfo = value;
    }

}
