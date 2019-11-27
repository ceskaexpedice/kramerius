<?xml version="1.1" encoding="UTF-8"?> 
<!-- $Id: demoFoxmlToLucene.xslt 5734 2006-11-28 11:20:15Z gertsp $ -->
<xsl:stylesheet version="1.1"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
        xmlns:fn="http://www.w3.org/2005/xpath-functions"
    	xmlns:exts="java://cz.incad.kramerius.indexer.FedoraOperations"
        xmlns:java="http://xml.apache.org/xslt/java"
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
    >
    <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" encoding="UTF-8" />

    <xsl:include href="res/biblio_location.xslt"/>

    <xsl:template match="/">
            <xsl:apply-templates mode="biblioMods" select="/mods:modsCollection/mods:mods" />
    </xsl:template>

    
     <xsl:template match="mods:modsCollection/mods:mods" mode="biblioMods">
        <xsl:for-each select="mods:language[not(@objectPart) or @objectPart != 'translation']/mods:languageTerm/text()">
        <field name="language">
            <xsl:value-of select="." />
        </field>
        <xsl:text>&#xa;</xsl:text>
        </xsl:for-each>
        <!-- <mods:location  -->
        <xsl:apply-templates select="mods:location" />
    </xsl:template>


</xsl:stylesheet>