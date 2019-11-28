<?xml version="1.1" encoding="UTF-8"?>
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

                exclude-result-prefixes="xsl fn exts java zs foxml dc oai_dc uvalibdesc uvalibadmin fedora-model rdf mods kramerius"
>
    <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" encoding="UTF-8" />

    <xsl:template match="/mods:modsCollection/mods:mods">
        <field name="details">

            <xsl:variable name="volumeName"><xsl:choose>
                <xsl:when test="mods:titleInfo/mods:partName">
                    <xsl:value-of select="mods:titleInfo/mods:partName" />
                </xsl:when>
            </xsl:choose>
            </xsl:variable>

            <xsl:variable name="volumeNumber"><xsl:choose>
                <xsl:when test="mods:titleInfo/mods:partNumber">
                    <xsl:value-of select="mods:titleInfo/mods:partNumber" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="mods:part/mods:detail[@type = 'volume']/mods:number" />
                </xsl:otherwise>
            </xsl:choose>
            </xsl:variable>

            <xsl:choose>
                <xsl:when test="mods:part/mods:date">
                    <xsl:value-of select="mods:part/mods:date" /><xsl:value-of select="'##'" />
                    <xsl:value-of select="$volumeNumber" />
                </xsl:when>
                <xsl:when test="mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()">
                    <xsl:value-of select="mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()" /><xsl:value-of select="'##'" />
                    <xsl:value-of select="$volumeNumber" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="mods:originInfo/mods:dateIssued" /><xsl:value-of select="'##'" />
                    <xsl:value-of select="$volumeNumber" />
                    <xsl:if test="$volumeName">
                        <xsl:value-of select="'##'" />
                        <xsl:value-of select="$volumeName" />
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </field>

    </xsl:template>



</xsl:stylesheet>