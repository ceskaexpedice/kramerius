<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fn="http://www.w3.org/2005/xpath-functions"
xmlns:sp="http://www.fedora.info/definitions/1/0/types/"
version="1.0" exclude-result-prefixes="fn sp">
    <xsl:output method="html" encoding="UTF-8" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="rows" select="'10'"/>
    <xsl:param name="offset" select="'0'"/>
    <xsl:param name="sort" select="'title'"/>
    <xsl:param name="sort_dir" select="'asc'"/>
    <xsl:param name="model" select="model"/>
    <xsl:template match="/">
        <xsl:variable name="q" >'</xsl:variable>
        <xsl:variable name="qescaped" ></xsl:variable>
        <xsl:for-each select="/sp:result/sp:resultList/sp:objectFields">
            <xsl:variable name="title" select="normalize-space(./sp:title)" />
            <xsl:variable name="titleescaped" select="translate($title, $q, $qescaped)" />
            <xsl:variable name="model" ><xsl:value-of select="concat('document.type.', substring-after(./sp:type, ':'))" /></xsl:variable>
            <xsl:variable name="date" select="normalize-space(./sp:date)" />
            <tr class="indexer_result"><xsl:attribute name="pid"><xsl:value-of select="./sp:pid" /></xsl:attribute>
            <td class="indexer_result_status">&#160;</td>
            <td width="100%"> - 
            <a title="index document"><xsl:attribute name="href">javascript:indexDoc('<xsl:value-of select="./sp:pid" />', '<xsl:value-of select="$titleescaped" />');</xsl:attribute><xsl:value-of select="./sp:title" />
            (<xsl:value-of select="$bundle/value[@key=$model]"/>)</a></td>
            <td width="240px" style="min-width:240px;" ><xsl:value-of select="./sp:pid"/></td>
            <td style="min-width:138px;"><xsl:value-of select="concat(
                      substring(./sp:mDate, 1, 10),
                      '&#160;',
                      substring(./sp:mDate, 12, 8))" /></td>
            </tr>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
