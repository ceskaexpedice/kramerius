<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:exts="java://cz.incad.utils.XslHelper"
    xmlns:java="http://xml.apache.org/xslt/java"
    xmlns:alto="http://www.loc.gov/standards/alto/ns-v2#"
    exclude-result-prefixes="exts java">
    <xsl:output  method="html"  />
    <xsl:param name="q" select="q" />
    <xsl:param name="w" select="w" />
    <xsl:param name="h" select="h" />
    <xsl:variable name="generic" select="exts:new()" />

    <xsl:template  match="/" >
        <xsl:variable name="page_height"><xsl:choose>
                <xsl:when test="/alto:alto/alto:Layout/alto:Page/@HEIGHT"><xsl:value-of select="/alto:alto/alto:Layout/alto:Page/@HEIGHT" /></xsl:when>
                <xsl:otherwise><xsl:value-of select="/alto:alto/alto:Layout/alto:Page/alto:PrintSpace/@HEIGHT" /></xsl:otherwise>
        </xsl:choose></xsl:variable>
        <xsl:variable name="coeficient" select="$h div number($page_height)"/>
        <xsl:for-each select="//alto:String">
            <xsl:if test="exts:contains($generic, ./@CONTENT, $q) or exts:contains($generic, ./@SUBS_CONTENT, $q)">
        <div class="alto_selection">
        <xsl:attribute name="style">
            position:absolute; left:<xsl:value-of select="./@HPOS *  $coeficient - 2" />px; top:<xsl:value-of select="./@VPOS * $coeficient - 2" />px;
            width:<xsl:value-of select="./@WIDTH *  $coeficient + 3" />px; height:<xsl:value-of select="./@HEIGHT *  $coeficient + 3" />px;
        </xsl:attribute>
        </div></xsl:if>
        </xsl:for-each>

        <xsl:variable name="page_height2"><xsl:choose>
            <xsl:when test="/alto/Layout/Page/@HEIGHT"><xsl:value-of select="/alto/Layout/Page/@HEIGHT" /></xsl:when>
            <xsl:otherwise><xsl:value-of select="/alto/Layout/Page/PrintSpace/@HEIGHT" /></xsl:otherwise>
        </xsl:choose></xsl:variable>
        <xsl:variable name="k2" select="$h div number($page_height2)"/>
        <xsl:for-each select="//String">
            <xsl:if test="exts:contains($generic, ./@CONTENT, $q) or exts:contains($generic, ./@SUBS_CONTENT, $q)">
        <div class="alto_selection">
        <xsl:attribute name="style">
            position:absolute; left:<xsl:value-of select="./@HPOS *  $k2 - 2" />px; top:<xsl:value-of select="./@VPOS * $k2 - 2" />px;
            width:<xsl:value-of select="./@WIDTH *  $k2 + 3" />px; height:<xsl:value-of select="./@HEIGHT *  $k2 + 3" />px;
        </xsl:attribute>
        </div></xsl:if>
        </xsl:for-each>
                
    </xsl:template>
</xsl:stylesheet>
