<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"
    xmlns:exts="java://cz.incad.utils.XSLFunctions"
    xmlns:java="http://xml.apache.org/xslt/java">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />
    <xsl:variable name="colsDoc"  select="exts:getCollectionsDoc()" />
    <xsl:template match="/">
        <add>
            <xsl:for-each select="//result/doc">
            <xsl:variable name="fedoraModel" select="./str[@name='fedora.model']" />
            <doc>
                <xsl:for-each select="str">
                    <field>
                        <xsl:attribute name="name">
                            <xsl:choose>
                                <xsl:when test="@name = 'dc.title'">title</xsl:when>
                                <xsl:when test="@name = 'dc.creator'">autor</xsl:when>
                                <xsl:when test="@name = 'dc.description'">dc_description</xsl:when>
                                <xsl:otherwise><xsl:value-of  select="exts:replace(@name, '\.', '_')"/></xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:value-of select="." />
                    </field>
                </xsl:for-each>
                <xsl:for-each select="int">
                    <field><xsl:attribute name="name"><xsl:value-of  select="@name"/></xsl:attribute><xsl:value-of select="." /></field>
                </xsl:for-each>
                <xsl:for-each select="date">
                    <xsl:choose>
                        <xsl:when test="@name = 'datum'">
                        <!-- FIX DATUM 1970-01-01 -->
                            <field name="datum"><xsl:value-of select="exts:fixDatum(../str[@name='datum_str'])" /></field>
                        </xsl:when>
                        <xsl:otherwise>
                            <field><xsl:attribute name="name"><xsl:value-of  select="@name"/></xsl:attribute><xsl:value-of select="." /></field>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
                <xsl:for-each select="bool">
                    <field><xsl:attribute name="name"><xsl:value-of  select="@name"/></xsl:attribute><xsl:value-of select="." /></field>
                </xsl:for-each>
                <xsl:for-each select="arr/str">
                    <xsl:choose>
                        <xsl:when test="../@name = 'collection'">
                            <field name="collection"><xsl:value-of select="." /></field>
                            
                            <xsl:for-each select="$colsDoc/collection[@pid = .]/desc">
                                <field ><xsl:attribute name="name">collection_<xsl:value-of  select="./lang"/></xsl:attribute><xsl:value-of select="./label" /></field>
                            </xsl:for-each>
                            
                        </xsl:when>
                        <xsl:when test="../@name = 'details'">
                            <field name="details"><xsl:value-of select="." /></field>
                            <field name="extinfo"><xsl:value-of select="exts:detailsToJson(., $fedoraModel)" /></field>
                        </xsl:when>
                        <xsl:otherwise>
                            <field>
                                <xsl:attribute name="name">
                                    <xsl:choose>
                                        <xsl:when test="../@name = 'dc.title'">title</xsl:when>
                                        <xsl:when test="../@name = 'dc.creator'">autor</xsl:when>
                                        <xsl:when test="../@name = 'dc.description'">dc_description</xsl:when>
                                        <xsl:when test="../@name = 'fedora.model'">fedora_model</xsl:when>
                                        <xsl:otherwise><xsl:value-of  select="exts:replace(../@name, '\.', '_')"/></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:value-of select="." />
                            </field>
                        </xsl:otherwise>
                    </xsl:choose>
                    
                </xsl:for-each>
                <xsl:for-each select="arr/int">
                    <field><xsl:attribute name="name"><xsl:value-of  select="../@name"/></xsl:attribute><xsl:value-of select="." /></field>
                </xsl:for-each>
                
                <xsl:variable name="title"><xsl:value-of select="str[@name='dc.title']" /></xsl:variable>
                
                <xsl:for-each select="arr[@name='dc.creator']">
                    <field name="browse_autor" ><xsl:value-of select="exts:prepareCzech(./str)"/>##<xsl:value-of select="./str" /></field>
                </xsl:for-each>
                
                <field name="browse_title" >
                    <xsl:value-of select="exts:prepareCzech($title)"/>##<xsl:value-of select="$title"/>
                </field>
            </doc>
            </xsl:for-each>
        </add>
    </xsl:template>

</xsl:stylesheet>