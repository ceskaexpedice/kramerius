<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fn="http://www.w3.org/2005/xpath-functions"
xmlns:sp="http://www.w3.org/2001/sw/DataAccess/rf1/result"
version="1.0">
    <xsl:output method="html" encoding="UTF-8" />
    <xsl:param name="rows" select="'10'"/>
    <xsl:param name="offset" select="'0'"/>
    <xsl:param name="sort" select="'title'"/>
    <xsl:param name="sort_dir" select="'asc'"/>
    <xsl:param name="model" select="model"/>
    <xsl:template match="/">
        <xsl:variable name="q" >'</xsl:variable>
        <xsl:variable name="qescaped" ></xsl:variable>
        <xsl:for-each select="/sp:sparql/sp:results/sp:result">
            <xsl:variable name="title" select="normalize-space(./sp:title)" />
            <xsl:variable name="titleescaped" select="translate($title, $q, $qescaped)" />
            <xsl:variable name="date" select="normalize-space(./sp:date)" />
            <tr class="indexer_result"><xsl:attribute name="pid"><xsl:value-of select="./sp:object/@uri" /></xsl:attribute>
            <td class="indexer_result_status"></td><td>
            - 
            <a><xsl:attribute name="href">javascript:indexDoc('<xsl:value-of select="./sp:object/@uri" />', '<xsl:value-of select="$titleescaped" />');</xsl:attribute><xsl:value-of select="./sp:title" /></a>
            </td><td width="138"><xsl:value-of select="./sp:date" /></td>
            </tr>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
