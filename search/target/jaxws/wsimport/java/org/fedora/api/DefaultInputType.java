
package org.fedora.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for defaultInputType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="defaultInputType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="fedora:defaultInputType"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "defaultInputType")
@XmlEnum
public enum DefaultInputType {

    @XmlEnumValue("fedora:defaultInputType")
    FEDORA_DEFAULT_INPUT_TYPE("fedora:defaultInputType");
    private final String value;

    DefaultInputType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DefaultInputType fromValue(String v) {
        for (DefaultInputType c: DefaultInputType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
