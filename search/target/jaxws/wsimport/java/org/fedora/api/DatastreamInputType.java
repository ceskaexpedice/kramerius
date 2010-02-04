
package org.fedora.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for datastreamInputType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="datastreamInputType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="fedora:datastreamInputType"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "datastreamInputType")
@XmlEnum
public enum DatastreamInputType {

    @XmlEnumValue("fedora:datastreamInputType")
    FEDORA_DATASTREAM_INPUT_TYPE("fedora:datastreamInputType");
    private final String value;

    DatastreamInputType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DatastreamInputType fromValue(String v) {
        for (DatastreamInputType c: DatastreamInputType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
