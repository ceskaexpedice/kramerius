<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  version="1.0" 
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                 xmlns:mods="http://www.loc.gov/mods/v3" 
                 xmlns:fmt="http://java.sun.com/jsp/jstl/fmt" >
    <xsl:output method="plain" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:template match="/">
        <xsl:for-each select="//doc">
            <div>
                <xsl:attribute name="id"><xsl:value-of select="./str[@name='PID']" /></xsl:attribute>
                <xsl:for-each select="./arr[@name='details']/str">
                    <xsl:value-of select="." />&#160;
                </xsl:for-each>
            </div>
        </xsl:for-each>
        
    </xsl:template>
</xsl:stylesheet>