<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
<xsl:output encoding='UTF-8' indent='yes' />
<xsl:template match="/">
<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3"> 
	
	<mods:mods version="3.4">
		<xsl:if test="/MonographUnit/UniqueIdentifier/UniqueIdentifierURNType">
			<mods:identifier type="urn"><xsl:value-of select="/MonographUnit/UniqueIdentifier/UniqueIdentifierURNType" /></mods:identifier>
		</xsl:if>
		<xsl:if test="/MonographUnit/UniqueIdentifier/UniqueIdentifierSICIType">
			<mods:identifier type="sici"><xsl:value-of select="/MonographUnit/UniqueIdentifier/UniqueIdentifierSICIType" /></mods:identifier>
		</xsl:if>
		<xsl:if test="/MonographUnit/ISBN">
			<mods:identifier type="isbn"><xsl:value-of select="/MonographUnit/ISBN" /></mods:identifier>
		</xsl:if>
			
		<mods:titleInfo>
			<mods:title><xsl:value-of select="/MonographUnit/Title/MainTitle" /></mods:title>
			<xsl:if test="/MonographUnit/Title/SubTitle">
				<mods:subTitle><xsl:value-of select="/MonographUnit/Title/SubTitle" /></mods:subTitle>
			</xsl:if>	
		</mods:titleInfo>
		
		<xsl:if test="/MonographUnit/Title/ParallelTitle">
			<mods:titleInfo type="alternative">
				<xsl:for-each select="/MonographUnit/Title/ParallelTitle">
					<mods:title><xsl:value-of select="." /></mods:title>
				</xsl:for-each>
			</mods:titleInfo>
		</xsl:if>

		<mods:subject>
			<xsl:for-each select="/MonographUnit/Keyword">
		    	<mods:topic>
			    	<xsl:value-of select="." />
			    </mods:topic>
		    </xsl:for-each>
			<xsl:if test="/MonographUnit/PhysicalDescription/Scale">
				<mods:cartographics>
			 		<mods:scale>
			 			<xsl:value-of select="/MonographUnit/PhysicalDescription/Scale" />
			 		</mods:scale>
				</mods:cartographics>
			</xsl:if>	
		</mods:subject>
			
		<!-- Creator -->
		<xsl:for-each select="/MonographUnit/Creator">
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
						<mods:roleTerm type="code">cre</mods:roleTerm>
						<mods:roleTerm type="text"><xsl:value-of select="@Role" /></mods:roleTerm>
					</mods:role>
				</mods:name>
			</xsl:if>
		</xsl:for-each>
		
		<!-- Contributor -->
		<xsl:for-each select="/MonographUnit/Contributor">		
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
					<mods:roleTerm type="code">ctb</mods:roleTerm>
					<mods:roleTerm type="text"><xsl:value-of select="@Role" /></mods:roleTerm>
				</mods:role>
			</mods:name>
		</xsl:for-each>
		
		<!-- Publisher -->
		<xsl:for-each select="/MonographUnit/Publisher">
			<mods:originInfo transliteration="publisher">
				<mods:place>
					<mods:placeTerm type="text"><xsl:value-of select="PlaceOfPublication" /></mods:placeTerm>
				</mods:place>
				<mods:publisher><xsl:value-of select="PublisherName" /></mods:publisher>
				<mods:dateIssued><xsl:value-of select="DateOfPublication" /></mods:dateIssued>
			</mods:originInfo>
		</xsl:for-each>

		<!-- Printer -->
		<xsl:for-each select="/MonographUnit/Printer">
			<mods:originInfo transliteration="printer">
				<mods:place>
					<mods:placeTerm><xsl:value-of select="PlaceOfPrinting" /></mods:placeTerm>
				</mods:place>
				<mods:publisher><xsl:value-of select="PrinterName" /></mods:publisher>
				<mods:dateCreated><xsl:value-of select="DateOfPrinting" /></mods:dateCreated>
			</mods:originInfo>
		</xsl:for-each>
		
		<mods:originInfo>
			<mods:issuance>monographic</mods:issuance>
		</mods:originInfo>
		
		<xsl:for-each select="/MonographUnit/Language">
			<mods:language>	
				<mods:languageTerm type="code" authority="iso639-2b"><xsl:value-of select="." /></mods:languageTerm>
			</mods:language>
		</xsl:for-each>
		
		<xsl:if test="/MonographUnit/PhysicalDescription">
			<mods:physicalDescription>
				<xsl:if test="/MonographUnit/PhysicalDescription/Technique">
					<mods:form type="technique"><xsl:value-of select="/MonographUnit/PhysicalDescription/Technique" /></mods:form>
				</xsl:if>
				<xsl:if test="/MonographUnit/PhysicalDescription/Material">
					<mods:form type="material"><xsl:value-of select="/MonographUnit/PhysicalDescription/Material" /></mods:form>
				</xsl:if>
				<xsl:if test="/MonographUnit/PhysicalDescription/Size">
					<mods:extent>
						<xsl:value-of select="/MonographUnit/PhysicalDescription/Size" />
					</mods:extent>
				</xsl:if>
				<xsl:if test="/MonographUnit/PhysicalDescription/Extent">
					<mods:extent>
						<xsl:value-of select="/MonographUnit/PhysicalDescription/Extent" />
					</mods:extent>
				</xsl:if>
				<xsl:if test="/MonographUnit/Notes">
					<mods:note><xsl:value-of select="/MonographUnit/Notes" /></mods:note>
				</xsl:if>
			</mods:physicalDescription>
		</xsl:if>
		
		<xsl:for-each select="/MonographUnit/PhysicalDescription/PreservationStatus">
			<mods:physicalDescription>
				<!-- PhysicalDescription/PreservationStatus -->
				<mods:note type="preservationStateOfArt"><xsl:value-of select="PreservationStateOfArt" /></mods:note>	
				<mods:note type="action"><xsl:value-of select="PreservationTreatment" /></mods:note>
			</mods:physicalDescription>
		</xsl:for-each>

		<!-- MonographUnit subject -->
		<xsl:for-each select="/MonographUnit/Subject">
			<mods:classification authority="ddc"><xsl:value-of select="./DDC" /></mods:classification>
			<mods:classification authority="udc"><xsl:value-of select="./UDC" /></mods:classification>
		</xsl:for-each>
		
		<mods:part>
			<xsl:attribute name="type"><xsl:value-of select="/MonographUnit/@Type" /></xsl:attribute>
			<mods:detail>
				<xsl:if test="/MonographUnit/MonographUnitIdentification/MonographUnitName">
					<mods:title><xsl:value-of select="/MonographUnit/MonographUnitIdentification/MonographUnitName" /></mods:title>
				</xsl:if>	
				<xsl:if test="/MonographUnit/MonographUnitIdentification/MonographUnitNumber">
					<mods:number><xsl:value-of select="/MonographUnit/MonographUnitIdentification/MonographUnitNumber" /></mods:number>
				</xsl:if>	
			</mods:detail>
		</mods:part>
		
		<xsl:if test="/MonographUnit/Accessibility">			
			<mods:accessCondition type="restrictionOnAccess"><xsl:value-of select="/MonographUnit/Accessibility" /></mods:accessCondition>
		</xsl:if>	
	</mods:mods>
</mods:modsCollection>
</xsl:template>
</xsl:stylesheet>