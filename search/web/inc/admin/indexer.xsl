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
            <xsl:choose>
                <xsl:when test="position() &gt; $rows">
                    <tr><td colspan="3">
                        <input id="indexer_result_rows" type="hidden"><xsl:attribute name="value" select="count(/sp:sparql/sp:results/sp:result)"/></input>
                    </td></tr>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="title" select="normalize-space(./sp:title)" />
                    <xsl:variable name="titleescaped" select="translate($title, $q, $qescaped)" />
                    <xsl:variable name="date" select="normalize-space(./sp:date)" />
                    <tr class="indexer_result"><xsl:attribute name="pid"><xsl:value-of select="./sp:object/@uri" /></xsl:attribute>
                    <td class="indexer_result_status">&#160;</td>
                    <td width="100%"> - 
                    <a title="index document"><xsl:attribute name="href">javascript:indexDoc('<xsl:value-of select="./sp:object/@uri" />', '<xsl:value-of select="$titleescaped" />');</xsl:attribute><xsl:value-of select="./sp:title" /></a>
                    </td>
                    <td width="240px" style="min-width:240px;" ><xsl:value-of select="substring-after(./sp:object/@uri, 'info:fedora/')"/></td>
                    <td style="min-width:138px;">
                        <xsl:value-of select="./sp:date" />
                    </td>
                    </tr>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
        
        
    </xsl:template>

</xsl:stylesheet>
