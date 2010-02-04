
package org.fedora.api;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ComparisonOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ComparisonOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="has"/>
 *     &lt;enumeration value="eq"/>
 *     &lt;enumeration value="lt"/>
 *     &lt;enumeration value="le"/>
 *     &lt;enumeration value="gt"/>
 *     &lt;enumeration value="ge"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ComparisonOperator")
@XmlEnum
public enum ComparisonOperator {

    @XmlEnumValue("has")
    HAS("has"),
    @XmlEnumValue("eq")
    EQ("eq"),
    @XmlEnumValue("lt")
    LT("lt"),
    @XmlEnumValue("le")
    LE("le"),
    @XmlEnumValue("gt")
    GT("gt"),
    @XmlEnumValue("ge")
    GE("ge");
    private final String value;

    ComparisonOperator(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ComparisonOperator fromValue(String v) {
        for (ComparisonOperator c: ComparisonOperator.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
