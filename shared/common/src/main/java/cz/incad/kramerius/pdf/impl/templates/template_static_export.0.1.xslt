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


        <xsl:output method="xml" indent="no" encoding="UTF-8" />
        
    <xsl:template match="/">
        <commands>
            <paragraph spacing-after="10">
                <textsarray>
                    <text font-formal-name="header4">
                        <xsl:value-of select="$parent_title"></xsl:value-of>
                </text>
                </textsarray>
            </paragraph>
            <break></break>

            <paragraph spacing-after="10">
                <textsarray><text font-formal-name="normal"> </text> </textsarray> 
        </paragraph>
            
            <xsl:apply-templates select="/mods:modsCollection"></xsl:apply-templates>
            <paragraph>
                <textsarray><text font-formal-name="normal"> </text> </textsarray> 
        </paragraph>
        </commands>
    </xsl:template>

    <xsl:template match="mods:modsCollection">
        <list>
    <xsl:apply-templates select="mods:mods/mods:titleInfo[not(@*)]"></xsl:apply-templates> 

        
        
        <xsl:apply-templates select="mods:mods/mods:identifier"></xsl:apply-templates>


        <!-- rendrovani autoru -->      
        <xsl:call-template name="authors-template"/>    
        <!-- renderovani vydavatelu -->     
        <xsl:call-template name="publishers-template"/> 
        </list>
    </xsl:template>


<!-- sablona pro autory-->  
<xsl:template name="authors-template">
<xsl:if test="count(mods:mods/mods:name[@type='personal']) &gt; 0"> 
<item  list-symbol=" ">
    <textsarray>
        <text font-formal-name="normal"><xsl:value-of select="$bundle/value[@key='pdf.authors']"/><xsl:text>:</xsl:text>
        </text>
        <text font-formal-name="normal"><xsl:for-each select="mods:mods/mods:name[@type='personal']"><xsl:value-of select="mods:namePart[@type='family']"/> <xsl:text> </xsl:text> <xsl:value-of select="mods:namePart[@type='given']"/>   <xsl:if test="position() != last()">,</xsl:if></xsl:for-each>
        </text>     
    </textsarray>
</item>

</xsl:if>

</xsl:template>

<!-- sablona pro vydavatele -->
<xsl:template name="publishers-template">
<xsl:if test="count(mods:mods/mods:originInfo[@transliteration='publisher']) &gt; 0"> 

<item  list-symbol=" ">
    <textsarray>
        <text font-formal-name="normal">
            <xsl:value-of select="$bundle/value[@key='pdf.publishers']"/><xsl:text>:</xsl:text>
        </text>
        <text font-formal-name="normal">
        <xsl:for-each select="mods:mods/mods:originInfo[@transliteration='publisher']">
        <xsl:value-of select="mods:publisher/text()"/><xsl:if test="mods:dateIssued">(<xsl:value-of select="mods:dateIssued/text()"/>)</xsl:if><xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each> 
        </text>
    </textsarray>
</item>

</xsl:if>        
</xsl:template>



    <xsl:template match="mods:identifier">
            <xsl:if test="@type='sici' and text()!=''"> 
                <item  list-symbol=" ">    
            <textsarray>
                <text font-formal-name="normal">SICI:</text>
            <text font-formal-name="normal"><xsl:value-of select="text()"/></text>
        </textsarray>
        </item>
            </xsl:if>

            <xsl:if test="@type='urn' and text()!=''"> 
            <item  list-symbol=" ">
            <textsarray>
                <text font-formal-name="normal">URN:</text>
            <text font-formal-name="normal"><xsl:value-of select="text()"/></text>
        </textsarray>
        </item>
            </xsl:if>
            <xsl:if test="@type='issn' and text()!=''"> 
                        <item  list-symbol=" ">
                        <textsarray>
                <text font-formal-name="normal">ISSN:</text>
            <text font-formal-name="normal"><xsl:value-of select="text()"/></text>
            </textsarray>
            </item>
            </xsl:if>

            <xsl:if test="@type='isbn' and text()!=''"> 
            <item  list-symbol=" ">
                         <textsarray>
                <text font-formal-name="normal">ISBN:</text>
            <text font-formal-name="normal"><xsl:value-of select="text()"/></text>                  
            </textsarray>
            </item>
                    
            </xsl:if>
    </xsl:template> 
    
    
    <!-- Sablona pro tituly -->
    <xsl:template name="title" match="mods:titleInfo">
    <item  list-symbol=" ">    
            <textsarray>
                <text font-formal-name="normal">
                    <xsl:value-of select="$bundle/value[@key='pdf.title']"/><xsl:text>: </xsl:text> <xsl:value-of select="mods:title/text()"/>
                </text>
            </textsarray>
    </item>         
    
        <xsl:if test="mods:subTitle/text()!=''">
    <item  list-symbol=" ">    
            <textsarray>
                <text font-formal-name="normal">
                <xsl:value-of select="$bundle/value[@key='pdf.subtitle']"/><xsl:text>:  </xsl:text> <xsl:value-of select="mods:subTitle/text()"/>
                </text>
            </textsarray>
    </item>         

        </xsl:if>

        <xsl:if test="mods:partName/text()!=''">
    <item  list-symbol=" ">    
            <textsarray>
                <text font-formal-name="normal">
            <xsl:value-of select="$bundle/value[@key='pdf.parttitle']"/><xsl:text>: </xsl:text> <xsl:value-of select="mods:partName/text()"/>
                </text>
            </textsarray>
    </item>         
        </xsl:if>
    </xsl:template>
    
    
    
    
    <!--  Typ zdroje -->
    <xsl:template name="type-of-resource" match="mods:typeOfResource">
        <xsl:if test="text()!=''">
        <xsl:call-template name="new-line"/>
<xsl:value-of select="$bundle/value[@key='pdf.typeofresource']"/><xsl:text>:    </xsl:text> <xsl:value-of select="text()"/>{font}(size=14)
        </xsl:if>
    </xsl:template>

    <!-- Nova radka --> 
    <xsl:template name="new-line">
<xsl:text>&#x0D;</xsl:text>
    </xsl:template>
    
</xsl:stylesheet>