<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"
    xmlns:java="http://xml.apache.org/xslt/java">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />
    <xsl:param name="root_title" select="root_title" />
    <xsl:template match="/">
        <add>
            <xsl:for-each select="/response/result/doc">
            <doc>
                <field name="PID"><xsl:value-of select="./str[@name='PID']"/></field>
                <field name="root_title" update="set"><xsl:value-of select="$root_title"/></field>
            </doc>
            </xsl:for-each>
        </add>
    </xsl:template>
</xsl:stylesheet>