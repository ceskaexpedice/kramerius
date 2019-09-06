<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:output omit-xml-declaration="yes"/>
    
    <!-- Setup Kramerius location variables -->
    <xsl:variable name="kramerius_url">https://localhost:8080/search/</xsl:variable>
    <xsl:variable name="api_point">api/v5.0/</xsl:variable>
    <xsl:variable name="oai_prefix">oai:kramerius.example.com:</xsl:variable>

    <!-- Initialize the Solr document variables -->
    <xsl:variable name="doc" select="//doc"/>
    <xsl:variable name="verb" select="//str[@name='verb']/text()"/>

    <xsl:template match="response">
        <xsl:choose>
            <xsl:when test="$verb='GetRecord'">
                <record>
                    <xsl:call-template name="header"/>
                    <xsl:call-template name="metadata"/>
                    <xsl:call-template name="about"/>
                </record>
            </xsl:when>
            <xsl:when test="$verb='ListRecords'">
                <record>
                    <xsl:call-template name="header"/>
                    <xsl:call-template name="metadata"/>
                    <xsl:call-template name="about"/>
                </record>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="header"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="datestamp">
        <xsl:param name="solrdate"/>
        <xsl:choose>
            <xsl:when test="string-length($solrdate) > 20">
                <xsl:value-of select="concat(substring($solrdate, 0, 20), 'Z')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$solrdate"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
