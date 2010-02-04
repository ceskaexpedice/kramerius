
package org.fedora.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for userInputType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="userInputType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="fedora:userInputType"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "userInputType")
@XmlEnum
public enum UserInputType {

    @XmlEnumValue("fedora:userInputType")
    FEDORA_USER_INPUT_TYPE("fedora:userInputType");
    private final String value;

    UserInputType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UserInputType fromValue(String v) {
        for (UserInputType c: UserInputType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
