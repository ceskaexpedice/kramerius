<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
<xsl:output encoding='UTF-8' indent='yes' />
<xsl:template match="/">
<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3"> 
	<mods:mods version="3.3">
		<mods:identifier type="urn"><xsl:value-of select="/PeriodicalPage/UniqueIdentifier/UniqueIdentifierURNType" /></mods:identifier>
		<mods:identifier type="sici"><xsl:value-of select="/PeriodicalPage/UniqueIdentifier/UniqueIdentifierSICIType" /></mods:identifier>
		
		<!-- 
		  - Current version of Kramerius contains texts only  
		  -->		
		<mods:typeOfResource>text</mods:typeOfResource>

		<mods:part>
			<xsl:attribute name="type"><xsl:value-of select="/PeriodicalPage/@Type" /></xsl:attribute>
		 	<mods:detail type="pageNumber">
				<mods:number><xsl:value-of select="/PeriodicalPage/PageNumber" /></mods:number>
			</mods:detail>	
			<mods:detail type="pageIndex">
				<mods:number><xsl:value-of select="/PeriodicalPage/@Index" /></mods:number>
			</mods:detail>
			<mods:text><xsl:value-of select="/PeriodicalPage/Notes" /></mods:text>	
		</mods:part>										
	</mods:mods>
</mods:modsCollection>
</xsl:template>
</xsl:stylesheet>