<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
<xsl:output encoding='UTF-8' indent='yes' />
<xsl:template match="/">
<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3"> 
	<mods:mods version="3.4">
		<xsl:if test="/PeriodicalPage/UniqueIdentifier/UniqueIdentifierURNType">
			<mods:identifier type="urn"><xsl:value-of select="/PeriodicalPage/UniqueIdentifier/UniqueIdentifierURNType" /></mods:identifier>
		</xsl:if>
		
		<xsl:if test="/PeriodicalPage/UniqueIdentifier/UniqueIdentifierSICIType">
			<mods:identifier type="sici"><xsl:value-of select="/PeriodicalPage/UniqueIdentifier/UniqueIdentifierSICIType" /></mods:identifier>
		</xsl:if>
	
		<!-- 
		  - Current version of Kramerius contains texts only  
		  -->		
		<mods:typeOfResource>text</mods:typeOfResource>

		<mods:part>
			<xsl:attribute name="type"><xsl:value-of select="/PeriodicalPage/@Type" /></xsl:attribute>
		 	<xsl:if test="/PeriodicalPage/PageNumber">
				<mods:detail type="pageNumber">
					<xsl:for-each select="/PeriodicalPage/PageNumber">
						<mods:number><xsl:value-of select="." /></mods:number>
					</xsl:for-each>
				</mods:detail>	
			</xsl:if>
			<xsl:if test="/PeriodicalPage/@Index">
				<mods:detail type="pageIndex">
					<mods:number><xsl:value-of select="/PeriodicalPage/@Index" /></mods:number>
				</mods:detail>
			</xsl:if>	
			<xsl:if test="/PeriodicalPage/Notes">
				<mods:text><xsl:value-of select="/PeriodicalPage/Notes" /></mods:text>
			</xsl:if>		
		</mods:part>										
	</mods:mods>
</mods:modsCollection>
</xsl:template>
</xsl:stylesheet>