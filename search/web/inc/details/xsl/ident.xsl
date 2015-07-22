<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xalan="http://xml.apache.org/xslt">
 <xsl:output method="html" omit-xml-declaration="yes" indent="yes"  xalan:indent-amount="4" />
    <xsl:template match="node()|@*">
      <xsl:copy>
        <xsl:apply-templates select="node()|@*" />
      </xsl:copy>
    </xsl:template>
    <xsl:template match="text()">
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>
</xsl:stylesheet>

