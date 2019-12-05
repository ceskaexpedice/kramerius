<?xml version="1.0" encoding="UTF-8"?>

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

    <xsl:import href="oai.xsl"/>

    <xsl:template name="header">
        <header>
            <identifier>
                <xsl:value-of select="concat($oai_prefix, $doc//str[@name='PID'])"/>
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