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
		
        <xsl:param name="pid" select="'$pid$'"></xsl:param>
		<xsl:param name="model" select="'$model$'"></xsl:param>
		<xsl:param name="parent_title" select="'$parent_title$'"></xsl:param>
		<xsl:param name="bundle" select="document('$bundle_url$')/bundle" />
		<xsl:include href="$template_folder$/template_static_export.0.1.xslt"/>		
</xsl:stylesheet>