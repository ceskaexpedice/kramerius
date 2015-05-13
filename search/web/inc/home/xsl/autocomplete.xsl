<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:param name="incl" select="incl" />
    <xsl:template match="/">
        <xsl:if test="count(/response/lst[@name='terms']/lst/int) &gt; 1">
        <xsl:for-each select="/response/lst[@name='terms']/lst/int">
            <xsl:if test="(position() &gt; 1) or ($incl = 'true')">
            <div>
                <xsl:attribute name="class">term r<xsl:value-of select="position() mod 2" /></xsl:attribute>
                - <span><xsl:value-of select="substring-after(./@name, '##')" /></span> (<xsl:value-of select="." />) 
            </div>
            </xsl:if>
        </xsl:for-each>
        <div class="more_terms">&#160;</div>
        </xsl:if>
        <xsl:for-each select="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst">
            <xsl:variable name="navName" >
                <xsl:value-of select="./@name"/>
            </xsl:variable>
            <xsl:call-template name="facet">
                <xsl:with-param name="facetname"><xsl:value-of select="./@name" /></xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        <xsl:if test="count(/response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst/int) &gt; 1">
        <div class="more_terms">&#160;</div>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="facet">
        <xsl:param name="facetname" />
        <xsl:for-each select="./int">
            <xsl:if test="(position() &gt; 1) or ($incl = 'true')">
            <div>
                <xsl:attribute name="class">term r<xsl:value-of select="position() mod 2" /></xsl:attribute>
                - <span><xsl:value-of select="substring-after(./@name, '##')" /></span> (<xsl:value-of select="." />) 
            </div>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
