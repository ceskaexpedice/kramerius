<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0"
                xmlns:saxon="http://saxon.sf.net/"
                exclude-result-prefixes="saxon">

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
           <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc.xsd" xmlns:dc="http://dublincore.org/documents/2012/06/14/dcmi-terms/?v=elements">
	      <xsl:copy-of select="document(concat($kramerius_url, $api_point, 'item/', $pid, '/streams/DC'))" />
         </oai_dc:dc> 
        </metadata>
    </xsl:template>

    <xsl:template name="about"/>

</xsl:stylesheet>
