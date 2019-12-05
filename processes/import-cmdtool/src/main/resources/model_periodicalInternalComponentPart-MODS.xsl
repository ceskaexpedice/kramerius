<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
<xsl:output encoding='UTF-8' indent='yes' />
<xsl:template match="/">
<mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3"> 
	
	<mods:mods version="3.4">
		<xsl:if test = "/PeriodicalInternalComponentPart/UniqueIdentifier/UniqueIdentifierURNType">
			<mods:identifier type="urn"><xsl:value-of select="/PeriodicalInternalComponentPart/UniqueIdentifier/UniqueIdentifierURNType" /></mods:identifier>
		</xsl:if>
			
		<xsl:if test = "/PeriodicalInternalComponentPart/UniqueIdentifier/UniqueIdentifierSICIType">
			<mods:identifier type="sici"><xsl:value-of select="/PeriodicalInternalComponentPart/UniqueIdentifier/UniqueIdentifierSICIType" /></mods:identifier>
		</xsl:if>
		<xsl:if test = "/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Title/Coden">
			<mods:identifier type="coden"><xsl:value-of select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Title/Coden" /></mods:identifier>
		</xsl:if>			
		
		<xsl:if test = "/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Keyword">
			<mods:subject>
				<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Keyword">
			    	<mods:topic>
				    	<xsl:value-of select="." />
				    </mods:topic>
				</xsl:for-each>
			</mods:subject>
		</xsl:if>
		
		<xsl:if test = "/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Annotation">
			<mods:abstract><xsl:value-of select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Annotation" /></mods:abstract>
		</xsl:if>
		
		<mods:titleInfo>
			<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Title/*">
				<xsl:if test="local-name()='MainTitle'">
					<mods:title><xsl:value-of select="." /></mods:title>
				</xsl:if>
				<xsl:if test="local-name()='SubTitle'">
					<mods:subTitle><xsl:value-of select="." /></mods:subTitle>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Series">
				<mods:partName><xsl:value-of select="." /></mods:partName>	
			</xsl:for-each>		
		</mods:titleInfo>

		<xsl:if test="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Title/ParallelTitle">
			<mods:titleInfo type="alternative">
				<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Title/ParallelTitle">
					<mods:title><xsl:value-of select="." /></mods:title>
				</xsl:for-each>
			</mods:titleInfo>
		</xsl:if>	
	
		<xsl:if test = "/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Title/KeyTitle">
			<mods:titleInfo type="uniform">
				<mods:title><xsl:value-of select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Title/KeyTitle" /></mods:title>
			</mods:titleInfo>
		</xsl:if>
		
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Title/SortingTitle">
			<mods:titleInfo type="alternative">
				<mods:title><xsl:value-of select="." /></mods:title>
			</mods:titleInfo>
		</xsl:for-each>
		
		<!--xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/GMD">
			<mods:originInfo>
				<mods:issuance><xsl:value-of select="." /></mods:issuance>
			</mods:originInfo>
		</xsl:for-each-->
		
		<!-- 
		  - Creator
		  - /Periodical/PeriodicalVolume/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Creator
		  - grupovani zajisteno pres name  
		  -->
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Creator">
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
		  - <xsl:value-of select="/Periodical/PeriodicalVolume/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Contributor  " />
		  -->
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Contributor">
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
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Publisher">
			<mods:originInfo transliteration="publisher">
				<mods:place>
					<mods:placeTerm type="text"><xsl:value-of select="PlaceOfPublication" /></mods:placeTerm>
				</mods:place>
				<mods:publisher><xsl:value-of select="PublisherName" /></mods:publisher>
				<mods:dateIssued><xsl:value-of select="DateOfPublication" /></mods:dateIssued>
			</mods:originInfo>
		</xsl:for-each>

		<!-- Printer -->
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Printer">
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
					
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Language">	
			<mods:language>
				<mods:languageTerm type="code" authority="iso639-2b">
					<xsl:value-of select="." /></mods:languageTerm>
			</mods:language>
		</xsl:for-each>
			
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/PhysicalDescription">	
			<mods:physicalDescription>
				<mods:form type="technique"><xsl:value-of select="./Technique" /></mods:form>	
				<mods:extent>
					<xsl:value-of select="./Extent" />
				</mods:extent>
				<mods:extent>
					<xsl:value-of select="./Size" />
				</mods:extent>
			</mods:physicalDescription>
		</xsl:for-each>	
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Notes">
			<mods:note><xsl:value-of select="." /></mods:note>
		</xsl:for-each>
				
		
		<!-- Periodical classification -->
		<xsl:for-each select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Subject">
			<mods:classification authority="ddc"><xsl:value-of select="./DDC" /></mods:classification>
			<mods:classification authority="udc"><xsl:value-of select="./UDC" /></mods:classification>
		</xsl:for-each>
		
			 
		<mods:part>
			<xsl:attribute name="type"><xsl:value-of select="/PeriodicalInternalComponentPart/@Type" /></xsl:attribute>
		 	<xsl:if test = "/PeriodicalInternalComponentPart/InternalComponentPartIdNumber">
				<mods:detail type="article">
			 		<mods:number><xsl:value-of select="/PeriodicalInternalComponentPart/InternalComponentPartIdNumber" /></mods:number>
			 	</mods:detail>
			</xsl:if>
		 	<xsl:if test = "/PeriodicalInternalComponentPart/PageReference">
				<mods:extent unit="pages">
					<mods:list><xsl:value-of select="/PeriodicalInternalComponentPart/PageReference" /></mods:list>
				</mods:extent>
			</xsl:if>
		 	<xsl:if test = "/PeriodicalInternalComponentPart/PageNumber">
				<mods:detail type="pageNumber">
					<mods:number><xsl:value-of select="/PeriodicalInternalComponentPart/PageNumber" /></mods:number>
				</mods:detail>
			</xsl:if>
		</mods:part>

		<xsl:if test = "/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Accessibility">
			<mods:accessCondition type="restrictionOnAccess"><xsl:value-of select="/PeriodicalInternalComponentPart/CoreBibliographicDescriptionPeriodical/Accessibility" /></mods:accessCondition>
		</xsl:if>
	</mods:mods>
</mods:modsCollection>

</xsl:template>
</xsl:stylesheet>