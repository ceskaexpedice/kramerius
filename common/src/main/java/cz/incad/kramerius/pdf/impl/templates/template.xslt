<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
        xmlns:fn="http://www.w3.org/2005/xpath-functions"
    	xmlns:exts="xalan://dk.defxws.fedoragsearch.server.GenericOperationsImpl"
        xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan exts"
        xmlns:zs="http://www.loc.gov/zing/srw/"
        xmlns:foxml="info:fedora/fedora-system:def/foxml#"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
        xmlns:uvalibdesc="http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd"
        xmlns:uvalibadmin="http://dl.lib.virginia.edu/bin/admin/admin.dtd/"
        xmlns:fedora-model="info:fedora/fedora-system:def/model#" 
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
        xmlns:mods="http://www.loc.gov/mods/v3" 
        xmlns:kramerius="http://www.nsdl.org/ontologies/relationships#"
		xmlns:kram4i18n="http://nothing_url">


        <xsl:output method="text" indent="no" encoding="UTF-8" />
    
        
	<xsl:template match="/">
		<xsl:value-of select="$parent_title"></xsl:value-of> {font}(size=20)
		<xsl:call-template name="new-line"/>
		<xsl:value-of select="$bundle/value[@key=concat('pdf.',$model)]"/> {font}(size=18)
{line}
		<xsl:apply-templates select="mods:modsCollection"></xsl:apply-templates>
	</xsl:template>

	<xsl:template match="mods:modsCollection">
		<xsl:apply-templates select="mods:mods/mods:titleInfo[not(@*)]"></xsl:apply-templates>
		<xsl:call-template name="new-line"/>
		<xsl:apply-templates select="mods:mods/mods:typeOfResource"></xsl:apply-templates>
{line}
		<xsl:apply-templates select="mods:mods/mods:identifier"></xsl:apply-templates>
		<!-- rendrovani autoru -->		
		<xsl:call-template name="authors-template"/>	
		<!-- renderovani vydavatelu -->		
		<xsl:call-template name="publishers-template"/>	
		
	</xsl:template>


<!-- sablona pro autory-->	
<xsl:template name="authors-template">
<xsl:call-template name="new-line"/>
<xsl:value-of select="$bundle/value[@key='pdf.authors']"/><xsl:text>:	</xsl:text>
<xsl:for-each select="mods:mods/mods:name[@type='personal']">
<xsl:value-of select="mods:namePart[@type='family']"/>  <xsl:value-of select="mods:namePart[@type='given']"/>   <xsl:if test="position() != last()">,</xsl:if>
</xsl:for-each>
</xsl:template>

<!-- sablona pro vydavatele -->
<xsl:template name="publishers-template">
<xsl:call-template name="new-line"/>
<xsl:value-of select="$bundle/value[@key='pdf.publishers']"/><xsl:text>:	</xsl:text>
<xsl:for-each select="mods:mods/mods:originInfo[@transliteration='publisher']">
		<xsl:value-of select="mods:publisher/text()"/><xsl:if test="mods:dateIssued">(<xsl:value-of select="mods:dateIssued/text()"/>)</xsl:if><xsl:if test="position() != last()">,</xsl:if>
		</xsl:for-each>
</xsl:template>



<xsl:template match="mods:identifier">
			<xsl:if test="@type='sici' and text()!=''"> 
SICI:<xsl:text>	</xsl:text> <xsl:value-of select="text()"/>
			</xsl:if>
			<xsl:if test="@type='urn' and text()!=''"> 
URN:<xsl:text>	</xsl:text> <xsl:value-of select="text()"/>
			</xsl:if>
			<xsl:if test="@type='issn' and text()!=''"> 
ISSN:<xsl:text>	</xsl:text> <xsl:value-of select="text()"/>
			</xsl:if>
			<xsl:if test="@type='isbn' and text()!=''"> 
ISBN:<xsl:text>	</xsl:text> <xsl:value-of select="text()"/>
			</xsl:if>
</xsl:template>	
	
	<!-- Sablona pro tituly -->
<xsl:template name="title" match="mods:titleInfo">
	  
<xsl:value-of select="$bundle/value[@key='pdf.title']"/><xsl:text>:	</xsl:text> <xsl:value-of select="mods:title/text()"/>
		<xsl:if test="mods:subTitle/text()!=''">
		<xsl:call-template name="new-line"/>
<xsl:value-of select="$bundle/value[@key='pdf.subtitle']"/><xsl:text>:	</xsl:text> <xsl:value-of select="mods:subTitle/text()"/>
		</xsl:if>

		<xsl:if test="mods:partName/text()!=''">
		<xsl:call-template name="new-line"/>
<xsl:value-of select="$bundle/value[@key='pdf.parttitle']"/><xsl:text>:	</xsl:text> <xsl:value-of select="mods:partName/text()"/>
		</xsl:if>
	</xsl:template>
	
	
	<!--  Typ zdroje -->
	<xsl:template name="type-of-resource" match="mods:typeOfResource">
		<xsl:if test="text()!=''">
		<xsl:call-template name="new-line"/>
<xsl:value-of select="$bundle/value[@key='pdf.typeofresource']"/><xsl:text>:	</xsl:text> <xsl:value-of select="text()"/>		
		</xsl:if>
	</xsl:template>

	<!-- Nova radka -->	
	<xsl:template name="new-line">
<xsl:text>&#x0D;</xsl:text>
	</xsl:template>
	
</xsl:stylesheet>
