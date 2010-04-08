<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:mods="http://www.loc.gov/mods/v3"
	version="1.0">
<xsl:output method="text" encoding="UTF-8"/>

<xsl:template match="/">
	Biblio mods .... 
	<xsl:apply-templates select="mods:modsCollection/mods:mods"></xsl:apply-templates>
</xsl:template>

<xsl:template match="mods:titleInfo">
	<xsl:if test="empty(@type)">
		Titulek:<xsl:value-of select="mods:title/text()"></xsl:value-of>
	</xsl:if>
</xsl:template>


</xsl:stylesheet>