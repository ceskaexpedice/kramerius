<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
<xsl:output encoding='UTF-8' indent='yes' />
<xsl:template match="/">
<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3"> 
	<mods:mods version="3.4">
		<xsl:if test="/MonographComponentPart/UniqueIdentifier/UniqueIdentifierURNType">
			<mods:identifier type="urn"><xsl:value-of select="/MonographComponentPart/UniqueIdentifier/UniqueIdentifierURNType" /></mods:identifier>
		</xsl:if>
		
		<xsl:if test="/MonographComponentPart/UniqueIdentifier/UniqueIdentifierSICIType">
			<mods:identifier type="sici"><xsl:value-of select="/MonographComponentPart/UniqueIdentifier/UniqueIdentifierSICIType" /></mods:identifier>
		</xsl:if>
					
		<mods:titleInfo>
			<mods:title><xsl:value-of select="/MonographComponentPart/Title/MainTitle" /></mods:title>
			<xsl:if test="/MonographComponentPart/Title/SubTitle">
				<mods:subTitle><xsl:value-of select="/MonographComponentPart/Title/SubTitle" /></mods:subTitle>
			</xsl:if>	
		</mods:titleInfo>
		
		<xsl:if test="/MonographComponentPart/Title/ParallelTitle">
			<mods:titleInfo type="alternative">
				<xsl:for-each select="/MonographComponentPart/Title/ParallelTitle">
					<mods:title><xsl:value-of select="." /></mods:title>
				</xsl:for-each>
			</mods:titleInfo>
		</xsl:if>
		
		<xsl:if test="/MonographComponentPart/Keyword">
			<mods:subject>
				<xsl:for-each select="/MonographComponentPart/Keyword">
			    	<mods:topic>
				    	<xsl:value-of select="." />
				    </mods:topic>
			    </xsl:for-each>	    
			</mods:subject>
		</xsl:if>
			
		<!-- 
		  - Creator
		  - /Monograph/MonographComponentPart/Creator/
		-->
		<xsl:for-each select="/MonographComponentPart/Creator">
			<xsl:if test="not(CreatorSurname='***Donator NF***')">
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
			</xsl:if>
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
		
		<xsl:if test="/MonographComponentPart/Notes">
			<mods:physicalDescription>	
				<mods:note><xsl:value-of select="/MonographComponentPart/Notes" /></mods:note>
			</mods:physicalDescription>
		</xsl:if>

		<!-- MonographComponentPart subject -->
		<xsl:for-each select="/MonographComponentPart/Subject">
			<mods:classification authority="ddc"><xsl:value-of select="./DDC" /></mods:classification>
			<mods:classification authority="udc"><xsl:value-of select="./UDC" /></mods:classification>
		</xsl:for-each>
		
		<mods:part>
			<xsl:attribute name="type"><xsl:value-of select="/MonographComponentPart/@Type" /></xsl:attribute>
			<xsl:if test="/MonographComponentPart/PageReference">
				<mods:extent unit="pages">
					<mods:list><xsl:value-of select="/MonographComponentPart/PageReference" /></mods:list>
				</mods:extent>
			</xsl:if>	
			<xsl:if test="/MonographComponentPart/PageNumber">
				<mods:detail type="pageNumber">
					<mods:number><xsl:value-of select="/MonographComponentPart/PageNumber" /></mods:number>
				</mods:detail>
			</xsl:if>	
			<xsl:if test="/MonographComponentPart/MonographComponentPartIdentification">	
				<mods:detail type="pageNumber">
					<mods:number><xsl:value-of select="/MonographComponentPart/MonographComponentPartIdentification" /></mods:number>
				</mods:detail>	
			</xsl:if>	
		</mods:part>
		
		<xsl:if test="/MonographComponentPart/Accessibility">
			<mods:accessCondition type="restrictionOnAccess"><xsl:value-of select="/MonographComponentPart/Accessibility" /></mods:accessCondition>
		</xsl:if>	
	</mods:mods>
</mods:modsCollection>
</xsl:template>
</xsl:stylesheet>