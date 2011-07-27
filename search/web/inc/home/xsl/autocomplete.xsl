<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:template match="/">
        <xsl:for-each select="/response/lst[@name='terms']/lst/int">
            <div>
                <xsl:attribute name="class">term r<xsl:value-of select="position() mod 2" /></xsl:attribute>
                - <span><xsl:value-of select="substring-after(./@name, '##')" /></span> (<xsl:value-of select="." />) </div>
        </xsl:for-each>
        <xsl:if test="/response/lst[@name='terms']/lst/int">
            <div class="more_terms"></div>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
