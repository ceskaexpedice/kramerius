<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:sp="http://www.w3.org/2001/sw/DataAccess/rf1/result"
version="1.0">
    <xsl:output method="html" encoding="UTF-8" />
    <xsl:param name="rows" select="'10'"/>
    <xsl:param name="offset" select="'0'"/>
    <xsl:param name="model" select="model"/>
    <xsl:template match="/">
        <xsl:for-each select="/sp:sparql/sp:results/sp:result">
            <xsl:variable name="title" select="normalize-space(./sp:title)" />
            <div class="indexer_result">- <a><xsl:attribute name="href">javascript:indexDoc('<xsl:value-of select="./sp:object/@uri" />', '<xsl:value-of select="$title" />');</xsl:attribute><xsl:value-of select="./sp:title" /></a></div>
            
        </xsl:for-each>
        <div class="indexer_pager">
            <xsl:if test="$offset>0"><a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', <xsl:value-of select="$offset - $rows" />)</xsl:attribute>previous</a></xsl:if>
            <xsl:if test="count(/sp:sparql/sp:results/sp:result)=$rows"><a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', <xsl:value-of select="$offset + $rows" />)</xsl:attribute>next</a></xsl:if>
        </div>
    </xsl:template>

</xsl:stylesheet>
