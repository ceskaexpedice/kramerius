<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:x="http://digit.nkp.cz/Monographs/DTD/1.0/">
<xsl:output encoding='UTF-8' indent='yes' />
<xsl:template match="/">
<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3"> 
	
	<mods:mods version="3.3">
		<mods:identifier type="urn"><xsl:value-of select="/MonographUnit/UniqueIdentifier/UniqueIdentifierURNType" /></mods:identifier>
		<mods:identifier type="sici"><xsl:value-of select="/MonographUnit/UniqueIdentifier/UniqueIdentifierSICIType" /></mods:identifier>
		<mods:identifier type="isbn"><xsl:value-of select="/MonographUnit/ISBN" /></mods:identifier>
			
		<mods:titleInfo>
			<mods:title><xsl:value-of select="/MonographUnit/Title/MainTitle" /></mods:title>
			<mods:subTitle><xsl:value-of select="/MonographUnit/Title/SubTitle" /></mods:subTitle>
		</mods:titleInfo>
		<mods:titleInfo type="alternative">
			<mods:title><xsl:value-of select="/MonographUnit/Title/ParallelTitle" /></mods:title>
		</mods:titleInfo>

		<mods:subject>
			<mods:topic>
		    	<xsl:value-of select="/MonographUnit/Keyword" />
		    </mods:topic>
		    
			<mods:cartographics>
		 		<mods:scale>
		 			<xsl:value-of select="/MonographUnit/PhysicalDescription/Scale" />
		 		</mods:scale>
			</mods:cartographics>
		</mods:subject>
			
		<!-- Creator -->
		<xsl:for-each select="/MonographUnit/Creator">
			<mods:name type="personal">
				<mods:namePart type="family">
					<xsl:value-of select="CreatorSurname" />
				</mods:namePart>
				<mods:namePart type="given">
					<xsl:value-of select="CreatorName" />
				</mods:namePart>
				<mods:role>
					<mods:roleTerm type="code">cre</mods:roleTerm>
					<mods:roleTerm type="text"><xsl:value-of select="@Role" /></mods:roleTerm>
				</mods:role>
			</mods:name>
		</xsl:for-each>
		
		<!-- Contributor -->
		<xsl:for-each select="/MonographUnit/Contributor">		
			<mods:name type="personal">
				<mods:namePart type="family">	
					<xsl:value-of select="ContributorSurname" />
				</mods:namePart>			
				<mods:namePart type="given">
					<xsl:value-of select="ContributorName" />
				</mods:namePart>
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
		
		<mods:physicalDescription>
			<mods:form type="technique"><xsl:value-of select="/MonographUnit/PhysicalDescription/Technique" /></mods:form>
			<mods:form type="material"><xsl:value-of select="/MonographUnit/PhysicalDescription/Material" /></mods:form>
			
			<mods:extent>
				<xsl:value-of select="/MonographUnit/PhysicalDescription/Size" />
				<xsl:if test="/MonographUnit/PhysicalDescription/Extent/text() and
						/MonographUnit/PhysicalDescription/Size/text()">,</xsl:if>
				<xsl:value-of select="/MonographUnit/PhysicalDescription/Extent " />
			</mods:extent>
			
			<mods:note><xsl:value-of select="/MonographUnit/Notes" /></mods:note>
		</mods:physicalDescription>

		<xsl:for-each select="/MonographUnit/PhysicalDescription/PreservationStatus">
			<mods:physicalDescription>
				<!-- PhysicalDescription/PreservationStatus -->
				<mods:note type="preservationStateOfArt"><xsl:value-of select="PreservationStateOfArt" /></mods:note>	
				<mods:note type="action"><xsl:value-of select="PreservationTreatment" /></mods:note>
			</mods:physicalDescription>
		</xsl:for-each>

		<!-- MonographUnit subject -->
		<mods:classification authority="ddc"><xsl:value-of select="/MonographUnit/Subject/DDC" /></mods:classification>
		<mods:classification authority="udc"><xsl:value-of select="/MonographUnit/Subject/UDC" /></mods:classification>

		<mods:part>
			<xsl:attribute name="type"><xsl:value-of select="/MonographUnit/@Type" /></xsl:attribute>
			<mods:detail>
				<mods:title><xsl:value-of select="/MonographUnit/MonographUnitIdentification/MonographUnitName" /></mods:title>
				<mods:number><xsl:value-of select="/MonographUnit/MonographUnitIdentification/MonographUnitNumber" /></mods:number>
			</mods:detail>
		</mods:part>
					
		<mods:accessCondition type="restrictionOnAccess"><xsl:value-of select="/MonographUnit/Accessibility" /></mods:accessCondition>
			
	</mods:mods>
</mods:modsCollection>
</xsl:template>
</xsl:stylesheet>