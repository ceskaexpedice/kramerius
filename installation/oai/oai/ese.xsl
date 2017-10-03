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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                xmlns:saxon="http://saxon.sf.net/"
		xmlns:ese="http://www.europeana.eu/schemas/ese/"
                exclude-result-prefixes="saxon"
                
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:dcterms="http://purl.org/dc/terms/"
		xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		xsi:schemaLocation="http://www.europeana.eu/schemas/ese/ http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd http://purl.org/dc/elements/1.1/ http://www.dublincore.org/schemas/xmls/qdc/dc.xsd http://purl.org/dc/terms/ http://www.dublincore.org/schemas/xmls/qdc/dcterms.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd"
    >

    <!-- Demonstrate how a Solr response can be mapped into an oai_dc format.

    The header values are taken from stored Lucene index fields as set in the schema.xml document:

        header/identifier:
        <field name="identifier" type="string" indexed="true" stored="true" required="true"/>

        header/datestamp
        <field name="datestamp" type="date" indexed="true" stored="true" required="true" default="NOW"/>

        header/setSpec
        <field name="theme" type="string" indexed="true" stored="true" required="true" multiValued="true" />

    The metadata element is created by taking a data dump from one particular index field called 'resource'
    which is then mapped into dc. We use an XSLT 2 processor for this. -->
    <xsl:import href="oai.xsl"/>

    <xsl:template name="header">
        <header>
            <identifier>
                oai:localhost:<xsl:value-of select="$doc//str[@name='PID']"/>
            </identifier>
            <datestamp>
                <xsl:call-template name="datestamp">
                    <xsl:with-param name="solrdate" select="$doc//date[@name='modified_date']"/>
                </xsl:call-template>
            </datestamp>
                <setSpec>
                    <xsl:value-of select="$doc//str[@name='fedora.model']"/>
                </setSpec>
        </header>
    </xsl:template>

    <xsl:template name="metadata">
        <xsl:variable name="pid" select="$doc//str[@name='PID']" />
	<metadata>
            <ese:record xsi:schemaLocation="http://www.europeana.eu/schemas/ese/ http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd http://purl.org/dc/elements/1.1/ http://www.dublincore.org/schemas/xmls/qdc/dc.xsd http://purl.org/dc/terms/ http://www.dublincore.org/schemas/xmls/qdc/dcterms.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
	      <xsl:copy-of select="document(concat($kramerius_url, 'api/v5.0/item/', $pid, '/streams/DC'))/oai_dc:dc/*" />
	      
	      <ese:object><xsl:value-of select="concat($kramerius_url, 'img?uuid=', $pid, '&amp;stream=IMG_THUMB&amp;action=GETRAW')" /></ese:object>

	      <ese:provider><!--FIXME: fill provider name-->N/A</ese:provider>
	      
	      <xsl:choose>
		<xsl:when test="false"/><!-- comment this and use lines below in case of multiple ese:type -->
	<!--        <xsl:when test="oai_dc:dc/dc:type/text() = 'model:sculpture'">-->
	<!--      <ese:type>IMAGE</ese:type>-->
	<!--        </xsl:when>-->
		<xsl:otherwise>
	      <ese:type>TEXT</ese:type>
		</xsl:otherwise>
	      </xsl:choose>
	      
	      <ese:isShownAt><xsl:value-of select="concat($kramerius_url, 'handle/', $pid)" /></ese:isShownAt>
	      <ese:isShownBy><xsl:value-of select="concat($kramerius_url, 'img?uuid=', $pid, '&amp;stream=IMG_FULL&amp;action=TRANSCODE&amp;outputFormat=PNG')" /></ese:isShownBy>
	    </ese:record>
        </metadata>
    </xsl:template>

    <xsl:template name="about"/>

</xsl:stylesheet>