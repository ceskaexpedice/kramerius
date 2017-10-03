<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ OAI4Solr exposes your Solr indexes by adding a OAI2 protocol handler.
  ~
  ~     Copyright (c) 2011-2014  International Institute of Social History
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:output omit-xml-declaration="yes"/>
    
    <!-- Setup Kramerius location variables -->
    <xsl:variable name="kramerius_url">http://localhost:8080/search/</xsl:variable>
    <xsl:variable name="api_point">api/v5.0/</xsl:variable>

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

    <!--
    Useful for debugging
        <xsl:template match="@*|node()">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:template>
    -->


</xsl:stylesheet>
