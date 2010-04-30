<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: demoUvalibdescToLucene.xslt 5716 2006-10-10 14:09:04Z gertsp $ -->
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:uvalibdesc="http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	
	<xsl:template name="uvalibdesc">
	
		<xsl:for-each select="foxml:datastream/foxml:datastreamVersion/foxml:xmlContent/uvalibdesc:desc/*">
			<xsl:if test="name()='uvalibdesc:agent'">
				<IndexField IFname="uva.agent" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="text()"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="name()='uvalibdesc:culture'">
				<IndexField IFname="uva.culture" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="text()"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="name()='uvalibdesc:place'">
				<IndexField IFname="uva.place" index="TOKENIZED" store="YES" termVector="NO">
					<xsl:value-of select="./*/text()"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>
	
	</xsl:template>
	
</xsl:stylesheet>	
