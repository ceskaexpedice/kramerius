<xsl:stylesheet  version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="facetname" select="facetname" />
    <xsl:param name="numOpenedRows" select="numOpenedRows" />
    
    <xsl:template match="/">
            <xsl:call-template name="facet"  />
    </xsl:template>

    <xsl:template name="facet">
        <div class="facet">
        <div class="ui-icon ui-icon-triangle-1-e"><xsl:value-of select="$bundle/value[@key=$facetname]" /></div>
        <ul>
        <xsl:for-each select="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name=$facetname]/int">
            <li><xsl:if test="position() &gt= $numOpenedRows">
                <xsl:attribute name="class">more_facets</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="@name" /> (<xsl:value-of select="." />)</li>
        </xsl:for-each>
        </ul>
        </div>
    </xsl:template>

</xsl:stylesheet>
