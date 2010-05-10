<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3">
<xsl:output encoding='UTF-8' indent='yes' />
<xsl:template match="/">
<mods:modsCollection> 
		
	<mods:mods version="3.3">
		<mods:identifier type="urn"><xsl:value-of select="/PeriodicalVolume/UniqueIdentifier/UniqueIdentifierURNType" /></mods:identifier>
		<mods:identifier type="sici"><xsl:value-of select="/PeriodicalVolume/UniqueIdentifier/UniqueIdentifierSICIType" /></mods:identifier>
		<mods:identifier type="coden"><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Title/Coden" /></mods:identifier>
					
		<mods:subject>
		    <mods:topic>
		    	<xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Keyword" />
		    </mods:topic>	   
		</mods:subject>
	
		<mods:abstract><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Annotation" /></mods:abstract>

		<mods:titleInfo>
			<mods:title><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Title/MainTitle" /></mods:title>
			<mods:subTitle><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Title/SubTitle" /></mods:subTitle>
			<mods:partName><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Series" /></mods:partName>		
		</mods:titleInfo>
		
		<mods:titleInfo type="alternative">
			<mods:title><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Title/ParallelTitle" /></mods:title>
		</mods:titleInfo>
		<mods:titleInfo type="uniform">
			<mods:title><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Title/KeyTitle" /></mods:title>
		</mods:titleInfo>
		
		<!-- 
		  - Creator
		  - /Periodical/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Creator
		  - grupovani zajisteno pres name  
		  -->
		<xsl:for-each select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Creator">
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
		  - /Periodical/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Contributor  
		  -->
		<xsl:for-each select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Contributor">
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

		<!-- 
		  - Current version of Kramerius contains texts only  
		  -->		
		<mods:typeOfResource>text</mods:typeOfResource>
	
		<!-- Publisher -->
		<xsl:for-each select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Publisher">
			<mods:originInfo transliteration="publisher">
				<mods:place>
					<mods:placeTerm type="text"><xsl:value-of select="PlaceOfPublication" /></mods:placeTerm>
				</mods:place>
				<mods:publisher><xsl:value-of select="PublisherName" /></mods:publisher>
				<mods:dateIssued><xsl:value-of select="DateOfPublication" /></mods:dateIssued>
			</mods:originInfo>
		</xsl:for-each>

		<!-- Printer -->
		<xsl:for-each select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Printer">
			<mods:originInfo transliteration="printer">
				<mods:place>
					<mods:placeTerm><xsl:value-of select="PlaceOfPrinting" /></mods:placeTerm>
				</mods:place>
				<mods:publisher><xsl:value-of select="PrinterName" /></mods:publisher>
				<mods:dateCreated><xsl:value-of select="DateOfPrinting" /></mods:dateCreated>
			</mods:originInfo>
		</xsl:for-each>		
		
		<mods:originInfo>
    		<mods:issuance>continuing</mods:issuance>
		</mods:originInfo>
		
		<xsl:for-each select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Language">
			<mods:language>
				<mods:languageTerm type="code" authority="iso639-2b">
					<xsl:value-of select="." /></mods:languageTerm>
			</mods:language>
		</xsl:for-each>
				
		<mods:physicalDescription>
			<mods:form type="technique"><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/PhysicalDescription/Technique" /></mods:form>	
			<mods:extent>
				<xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/PhysicalDescription/Extent" />
				<xsl:if test="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/PhysicalDescription/Extent/text() and
						/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/PhysicalDescription/Size/text()">,</xsl:if>
				<xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/PhysicalDescription/Size" />
			</mods:extent>
			<mods:note><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Notes" /></mods:note>		 
		</mods:physicalDescription>
		

		<!-- Periodical classification -->
		<mods:classification authority="ddc"><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Subject/DDC" /></mods:classification>	
		<mods:classification authority="udc"><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Subject/UDC" /></mods:classification>
		
		<mods:part>
		 	<mods:detail type="volume">
		 		<mods:number><xsl:value-of select="/PeriodicalVolume/PeriodicalVolumeIdentification/PeriodicalVolumeNumber" /></mods:number>		
		 	</mods:detail>
		 	<mods:date><xsl:value-of select="/PeriodicalVolume/PeriodicalVolumeIdentification/PeriodicalVolumeDate" /></mods:date>
		 	<mods:text><xsl:value-of select="/PeriodicalVolume/PeriodicalVolumeIdentification/Defects" /></mods:text>
		</mods:part>
							
		<mods:accessCondition type="restrictionOnAccess"><xsl:value-of select="/PeriodicalVolume/CoreBibliographicDescriptionPeriodical/Accessibility" /></mods:accessCondition>
						
	</mods:mods>
</mods:modsCollection>

</xsl:template>
</xsl:stylesheet>