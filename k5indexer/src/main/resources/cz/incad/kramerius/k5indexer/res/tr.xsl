<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"
    xmlns:exts="java://cz.incad.utils.XSLFunctions"
    xmlns:java="http://xml.apache.org/xslt/java">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />
    
    <xsl:variable name="xslfunctions" select="exts:new('http://cdk-test.lib.cas.cz/search', '', '')" />
    <xsl:variable name="colsDoc"  select="exts:getCollectionsDoc($xslfunctions)" />
    <!--xsl:variable name="colsDoc"  select="document($colsDoc1)/collections" /-->
    <xsl:template match="/">
        <add>
            <xsl:for-each select="/response/result/doc">
            <xsl:variable name="fedoraModel" select="./str[@name='fedora.model']" />
            <doc>
                <xsl:for-each select="str">
                    <field>
                        <xsl:attribute name="name">
                            <xsl:choose>
                                <xsl:when test="@name = 'dc.title'">title</xsl:when>
                                <xsl:when test="@name = 'dc.creator'">autor</xsl:when>
                                <xsl:when test="@name = 'dc.description'">dc_description</xsl:when>
                                <xsl:otherwise><xsl:value-of  select="exts:replace($xslfunctions, @name, '\.', '_')"/></xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:value-of select="." />
                    </field>
                </xsl:for-each>
                <xsl:for-each select="int">
                    <field><xsl:attribute name="name"><xsl:value-of  select="@name"/></xsl:attribute><xsl:value-of select="." /></field>
                </xsl:for-each>
                <xsl:for-each select="date">
                    <field><xsl:attribute name="name"><xsl:value-of  select="@name"/></xsl:attribute><xsl:value-of select="." /></field>
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
                            <field name="extinfo"><xsl:value-of select="exts:detailsToJson($xslfunctions, ., $fedoraModel)" /></field>
                        </xsl:when>
                        <xsl:otherwise>
                            <field>
                                <xsl:attribute name="name">
                                    <xsl:choose>
                                        <xsl:when test="../@name = 'dc.title'">title</xsl:when>
                                        <xsl:when test="../@name = 'dc.creator'">autor</xsl:when>
                                        <xsl:when test="../@name = 'dc.description'">dc_description</xsl:when>
                                        <xsl:when test="../@name = 'fedora.model'">fedora_model</xsl:when>
                                        <xsl:otherwise><xsl:value-of  select="exts:replace($xslfunctions, ../@name, '\.', '_')"/></xsl:otherwise>
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
                    <field name="browse_autor" ><xsl:value-of select="exts:prepareCzech($xslfunctions, ./str)"/>##<xsl:value-of select="./str" /></field>
                </xsl:for-each>
                
                <field name="browse_title" >
                    <xsl:value-of select="exts:prepareCzech($xslfunctions, $title)"/>##<xsl:value-of select="$title"/>
                </field>
            </doc>
            </xsl:for-each>
        </add>
    </xsl:template>
    
    
    <xsl:template name="details">
        <xsl:param name="model"/>
        <xsl:param name="val"/>
    </xsl:template>
    
<!--
 ISO-8859-1 based URL-encoding demo
       Written by Mike J. Brown, mike@skew.org.
       Updated 2002-05-20.

       No license; use freely, but credit me if reproducing in print.

       Also see http://skew.org/xml/misc/URI-i18n/ for a discussion of
       non-ASCII characters in URIs.
  
    -->
    <!--
     The string to URL-encode.
           Note: By "iso-string" we mean a Unicode string where all
           the characters happen to fall in the ASCII and ISO-8859-1
           ranges (32-126 and 160-255) 
    -->

    <xsl:template name="url-encode">
        <xsl:param name="str"/>
    <!--
     Characters we'll support.
           We could add control chars 0-31 and 127-159, but we won't. 
    -->
        <xsl:variable name="ascii"> !"K$%K'()*+,-./0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
        <xsl:variable name="latin1">¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ</xsl:variable>
        <!--  Characters that usually don't need to be escaped  -->
        <xsl:variable name="safe">!'()*-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~</xsl:variable>
        <xsl:variable name="hex">0123456789ABCDEF</xsl:variable>
        <xsl:if test="$str">
            <xsl:variable name="first-char" select="substring($str,1,1)"/>
            <xsl:choose>
                <xsl:when test="contains($safe,$first-char)">
                    <xsl:value-of select="$first-char"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="codepoint">
                        <xsl:choose>
                            <xsl:when test="contains($ascii,$first-char)">
                                <xsl:value-of select="string-length(substring-before($ascii,$first-char)) + 32"/>
                            </xsl:when>
                            <xsl:when test="contains($latin1,$first-char)">
                                <xsl:value-of select="string-length(substring-before($latin1,$first-char)) + 160"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:message terminate="no">
                                    Warning: string contains a character that is out of range! Substituting "?".
</xsl:message>
                                <xsl:text>63</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="hex-digit1" select="substring($hex,floor($codepoint div 16) + 1,1)"/>
                    <xsl:variable name="hex-digit2" select="substring($hex,$codepoint mod 16 + 1,1)"/>
                    <xsl:value-of select="concat('%',$hex-digit1,$hex-digit2)"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="string-length($str) > 1">
                <xsl:call-template name="url-encode">
                    <xsl:with-param name="str" select="substring($str,2)"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
    </xsl:template>



</xsl:stylesheet>