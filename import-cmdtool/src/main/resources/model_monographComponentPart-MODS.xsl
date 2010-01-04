<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-3.xsd">
<xsl:output encoding='UTF-8' indent='yes' />
<xsl:template match="/">
<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3"> 
	<mods:mods version="3.3">
		<mods:identifier type="urn"><xsl:value-of select="/MonographComponentPart/UniqueIdentifier/UniqueIdentifierURNType" /></mods:identifier>
		<mods:identifier type="sici"><xsl:value-of select="/MonographComponentPart/UniqueIdentifier/UniqueIdentifierSICIType" /></mods:identifier>
					
		<mods:titleInfo>
			<mods:title><xsl:value-of select="/MonographComponentPart/Title/MainTitle" /></mods:title>
			<mods:subTitle><xsl:value-of select="/MonographComponentPart/Title/SubTitle" /></mods:subTitle>
		</mods:titleInfo>
		<mods:titleInfo type="alternative">
			<mods:title><xsl:value-of select="/MonographComponentPart/Title/ParallelTitle" /></mods:title>
		</mods:titleInfo>

		<mods:subject>
			<mods:topic>
		    	<xsl:value-of select="/MonographComponentPart/Keyword" />
		    </mods:topic>	    
		</mods:subject>
			
		<!-- 
		  - Creator
		  - /Monograph/MonographComponentPart/Creator/
		-->
		<xsl:for-each select="/MonographComponentPart/Creator">
			<mods:name type="personal">
				<mods:namePart type="family">
					<xsl:value-of select="CreatorSurname" />
				</mods:namePart>
				<xsl:for-each select="CreatorName">
					<mods:namePart type="given">
						<xsl:value-of select="." />
					</mods:namePart>
				</xsl:for-each>
				<mods:role>
					<!-- NOTE: Pouzit kod pro tag a text pro atribut -->
					<mods:roleTerm type="code">cre</mods:roleTerm>
					<mods:roleTerm type="text"><xsl:value-of select="@Role" /></mods:roleTerm>
				</mods:role>
			</mods:name>
		</xsl:for-each>
		
		<!-- 
		  - Contributor
		  - /Monograph/MonographComponentPart/Contributor/
		-->
		
		<xsl:for-each select="/MonographComponentPart/Contributor">
			<mods:name type="personal">
				<mods:namePart type="family">	
					<xsl:value-of select="ContributorSurname" />
				</mods:namePart>
				<xsl:for-each select="ContributorName">
					<mods:namePart type="given">
						<xsl:value-of select="." />
					</mods:namePart>
				</xsl:for-each>
				<mods:role>
					<!-- NOTE: Pouzit kod pro tag a text pro atribut -->
					<mods:roleTerm type="code">ctb</mods:roleTerm>
					<mods:roleTerm type="text"><xsl:value-of select="@Role" /></mods:roleTerm>	
				</mods:role>
			</mods:name>
		</xsl:for-each>	
				
		<xsl:for-each select="/MonographComponentPart/Language">
			<mods:language>	
				<mods:languageTerm type="code" authority="iso639-2b"><xsl:value-of select="." /></mods:languageTerm>
			</mods:language>
		</xsl:for-each>
		
		<mods:physicalDescription>	
			<mods:note><xsl:value-of select="/MonographComponentPart/Notes" /></mods:note>
		</mods:physicalDescription>

		<!-- MonographComponentPart subject -->
		<mods:classification authority="ddc"><xsl:value-of select="/MonographComponentPart/Subject/DDC" /></mods:classification>
		<mods:classification authority="udc"><xsl:value-of select="/MonographComponentPart/Subject/UDC" /></mods:classification>
		
		<mods:part>
			<xsl:attribute name="type"><xsl:value-of select="/MonographComponentPart/@Type" /></xsl:attribute>
			<mods:extent unit="pages">
				<mods:list><xsl:value-of select="/MonographComponentPart/PageReference" /></mods:list>
			</mods:extent>
			<mods:detail type="pageNumber">
				<mods:number><xsl:value-of select="/MonographComponentPart/PageNumber" /></mods:number>
			</mods:detail>	
		</mods:part>
		
		<mods:accessCondition type="restrictionOnAccess"><xsl:value-of select="/MonographComponentPart/Accessibility" /></mods:accessCondition>
	</mods:mods>
</mods:modsCollection>
</xsl:template>
</xsl:stylesheet>