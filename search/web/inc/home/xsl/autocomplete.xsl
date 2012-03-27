<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:param name="incl" select="incl" />
    <xsl:template match="/">
        <xsl:if test="count(/response/lst[@name='terms']/lst/int) &gt; 1">
        <xsl:for-each select="/response/lst[@name='terms']/lst/int">
            <xsl:if test="position() &gt; 1 or $incl = 'true'">
            <div>
                <xsl:attribute name="class">term r<xsl:value-of select="position() mod 2" /></xsl:attribute>
                - <span><xsl:value-of select="substring-after(./@name, '##')" /></span> (<xsl:value-of select="." />) 
            </div>
            </xsl:if>
        </xsl:for-each>
        <div class="more_terms">&#160;</div>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
