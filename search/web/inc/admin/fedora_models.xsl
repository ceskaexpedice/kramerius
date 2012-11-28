<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fn="http://www.w3.org/2005/xpath-functions"
xmlns:sp="http://www.w3.org/2001/sw/DataAccess/rf1/result"
version="1.0">
    <xsl:output method="html" encoding="UTF-8" />
    <xsl:template match="/">
        <xsl:variable name="q" >'</xsl:variable>
        <xsl:variable name="qescaped" ></xsl:variable>
        <select id="top_models_select" onChange="loadFedoraDocuments($('#top_models_select').val(), 0, '');">
            <option>--</option>
        <xsl:for-each select="/sp:sparql/sp:results/sp:result">
            <xsl:variable name="title" select="normalize-space(./sp:title)" />
            <xsl:variable name="titleescaped" select="translate($title, $q, $qescaped)" />
            <xsl:if test="contains(./sp:object/@uri,'info:fedora/model:')">
            <option>
                <xsl:attribute name="value"><xsl:value-of select="substring-after(./sp:object/@uri,'info:fedora/model:')" /></xsl:attribute>
                <xsl:value-of select="$titleescaped" />
            </option>
            </xsl:if>
        </xsl:for-each>
        </select>
    </xsl:template>

</xsl:stylesheet>
