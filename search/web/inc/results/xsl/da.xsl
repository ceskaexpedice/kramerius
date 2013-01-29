<xsl:stylesheet  version="1.0"
    xmlns:exts="java://cz.incad.utils.XslHelper"
    xmlns:java="http://xml.apache.org/xslt/java"
    xmlns:math="http://exslt.org/math"
    exclude-result-prefixes="exts java"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="cfgmin" select="cfgmin" />
    <xsl:param name="cfgmax" select="cfgmax" />
    <xsl:variable name="generic" select="exts:new()" />
    <xsl:variable name="maxv"><xsl:value-of select="math:max(response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='rok']/int[not(@name='0')])" /></xsl:variable>
    
    <xsl:template match="/">
        <xsl:variable name="miny"><xsl:value-of select="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='rok']/int[not(@name='0')][position()=1]/@name" /></xsl:variable>
        <xsl:variable name="minyear"><xsl:choose>
            <xsl:when test="number($miny) &lt; number($cfgmin)"><xsl:value-of select="$cfgmin" /></xsl:when>
            <xsl:otherwise><xsl:value-of select="substring($miny, 1, 3)" />0</xsl:otherwise>
        </xsl:choose></xsl:variable>
        
        <xsl:variable name="maxy"><xsl:value-of select="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='rok']/int[last()]/@name" /></xsl:variable>
        <xsl:variable name="maxyear"><xsl:choose>
            <xsl:when test="number($maxy) &gt; number($cfgmax)"><xsl:value-of select="$cfgmax" /></xsl:when>
            <xsl:otherwise><xsl:value-of select="$maxy" /></xsl:otherwise>
        </xsl:choose></xsl:variable>
        
        <xsl:call-template name="years">
            <xsl:with-param name="i"><xsl:value-of select="number($minyear)" /></xsl:with-param>
            <xsl:with-param name="max"><xsl:value-of select="number($maxyear)" /></xsl:with-param>
       </xsl:call-template>
       <script type="text/javascript">
        var number_of_items = <xsl:value-of select="count(response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='rok']/int[not(@name='0')])" />;
        var zooms = new Array();
        zooms[0] = [<xsl:value-of select="number($minyear)" />, <xsl:value-of select="number($maxyear)" />];
        zooms[1] = [<xsl:value-of select="number($minyear)" />, <xsl:value-of select="number($maxyear)" />];
        var startTime = <xsl:value-of select="number($minyear)" />;
        var endTime = <xsl:value-of select="number($maxyear)" />;
        firstBar = startTime;
        lastBar = <xsl:value-of select="number($maxyear)" />;
        </script>
    </xsl:template>
    
    <xsl:template name="years">
        <xsl:param name="i" />
        <xsl:param name="max" />
        <xsl:variable name="len" select="number(string-length($i)) - 1" />
        <xsl:variable name="d1"><xsl:value-of select="substring(string($i), 1, $len)" /></xsl:variable>
        <div class="da_group">
            <xsl:attribute name="id">da_group_<xsl:value-of select="$d1" /></xsl:attribute>
            <div class="da_group_title"><xsl:value-of select="$d1" />0 - <xsl:value-of select="$d1" />9</div>
            <div class="da_space"></div>
           <xsl:call-template name="decade">
                <xsl:with-param name="i"><xsl:value-of select="1" /></xsl:with-param>
                <xsl:with-param name="y"><xsl:value-of select="$i" /></xsl:with-param>
           </xsl:call-template>
        </div>
           
        <xsl:if test="$max &gt;= ($i + 10)">
        <xsl:call-template name="years">
            <xsl:with-param name="i"><xsl:value-of select="$i + 10" /></xsl:with-param>
            <xsl:with-param name="max"><xsl:value-of select="$max" /></xsl:with-param>
       </xsl:call-template>
       </xsl:if>
    </xsl:template>

    <xsl:template name="decade">
        <xsl:param name="i" />
        <xsl:param name="y" />
        <xsl:call-template name="year">
            <xsl:with-param name="y"><xsl:value-of select="$y" /></xsl:with-param>
        </xsl:call-template>
           <xsl:if test="10 &gt; $i">
           <xsl:call-template name="decade">
                <xsl:with-param name="i"><xsl:value-of select="$i + 1" /></xsl:with-param>
                <xsl:with-param name="y"><xsl:value-of select="$y + 1" /></xsl:with-param>
           </xsl:call-template>
           </xsl:if>
    </xsl:template>

    <xsl:template name="year">
        <xsl:param name="y" />
        <xsl:variable name="c">
            <xsl:choose>
            <xsl:when test="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='rok']/int[@name=$y]">
                <xsl:value-of select="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='rok']/int[@name=$y]" />
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose>
        </xsl:variable>
        <xsl:variable name="w">
        <xsl:choose>
            <xsl:when test="not($c = '0')">
                <xsl:value-of select="floor(number($c) * 100 div number($maxv))" />%
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose></xsl:variable>
        <div class="da_bar_container">
            <xsl:attribute name="id">da_bar_container_<xsl:value-of select="$y" /></xsl:attribute>
            <div class="da_bar ui-widget-header">
                <xsl:attribute name="id">da_bar_<xsl:value-of select="$y" /></xsl:attribute>
                <xsl:attribute name="style">width: <xsl:value-of select="$w" />;</xsl:attribute><xsl:value-of select="$c" />
            </div>
        </div><div class="da_space"></div>
    </xsl:template>
</xsl:stylesheet>
