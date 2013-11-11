<xsl:stylesheet  version="1.0"
    xmlns:exts="java://cz.incad.utils.XslHelper"
    xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="exts java"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="fqs" select="fqs" />
    <xsl:param name="q" select="q" />
    <xsl:param name="cols" select="cols" />
    <xsl:param name="numOpenedRows" select="5" />
    <xsl:variable name="numDocs"><xsl:choose>
        <xsl:when test="/response/lst[@name='grouped']"><xsl:value-of select="number(/response/lst[@name='grouped']/lst/int[@name='matches'])" /></xsl:when>
        <xsl:otherwise><xsl:value-of select="number(/response/result/@numFound)" /></xsl:otherwise>
    </xsl:choose></xsl:variable>
    <xsl:variable name="numGroups"><xsl:choose>
        <xsl:when test="/response/lst[@name='grouped']"><xsl:value-of select="number(/response/lst[@name='grouped']/lst/int[@name='ngroups'])" /></xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose></xsl:variable>
    <xsl:variable name="generic" select="exts:new()" />
    
    <xsl:template match="/">
        <div class="loading_docs" style="position:absolute; z-index:3;margin:auto;background:white;border:1px silver;width:1000px;height:200px;">
            <img src="img/loading.gif" />
        </div>
        <xsl:variable name="rows"><xsl:value-of select="number(/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows'])" /></xsl:variable>
        <xsl:variable name="start">
            <xsl:choose>
                <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']/text()">
                    <xsl:value-of select="number(/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start'])" />
                </xsl:when>
                <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>

        </xsl:variable>
        <xsl:choose>   
        <xsl:when test="//result/doc" >
            <xsl:choose>
                <xsl:when test="$start = 0">
                    <xsl:call-template name="head" />
                    <div class="clear"></div>
                </xsl:when>
            </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
            <xsl:if test="$start = 0">
                <div style="font-weight:bold;"><xsl:value-of select="$bundle/value[@key='results.nohits']"/></div>
                <div class="clear"></div>
            </xsl:if>
        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="head">
        <xsl:variable name="numDocsStr">
            <xsl:choose>
                <xsl:when test="$numDocs = 1"><xsl:value-of select="$bundle/value[@key='common.documents.singular']"/></xsl:when>
                <xsl:when test="$numDocs &gt; 1 and $numDocs &lt; 5"><xsl:value-of select="$bundle/value[@key='common.documents.plural_1']"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$bundle/value[@key='common.documents.plural_2']"/></xsl:otherwise>
            </xsl:choose>
            <xsl:if test="$numGroups &gt; 0">
                &#160;<xsl:value-of select="$bundle/value[@key='results.collapsed.to']"/>&#160;<xsl:value-of select="$numGroups" />
            </xsl:if>
        </xsl:variable>
        <div class="header">
            <table style="width:100%;">
                <tr>
            <td style="width:200px;text-align:left;padding-left:5px;">
                <span><xsl:value-of select="$numDocs" />&#160;<xsl:value-of select="$numDocsStr" /></span>
            </td>
            <td align="center">
                <span><xsl:value-of select="$bundle/value[@key='results.sortby']"/>:</span>&#160;&#160;
                <xsl:variable name="sortParam" select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='sort']" />
                <xsl:choose>
                    <xsl:when test="contains($sortParam,'title_sort asc')">
                        <a href="javascript:sortByTitle('desc');"  style="font-weight:bolder;"><xsl:value-of select="$bundle/value[@key='results.sortby.name']"/></a>&#160;
                        <span class="ui-icon ui-icon-triangle-1-n"  >asc</span>
                        <xsl:if test="$q">
                            <span>&#160;|&#160;</span><a href="javascript:sortByRank();"><xsl:value-of select="$bundle/value[@key='results.sortby.relevance']"/></a>
                        </xsl:if>
                    </xsl:when>
                    <xsl:when test="contains($sortParam,'title_sort desc')">
                        <a href="javascript:sortByTitle('asc');"  style="font-weight:bolder;"><xsl:value-of select="$bundle/value[@key='results.sortby.name']"/></a>&#160;
                        <span class="ui-icon ui-icon-triangle-1-s"  >desc</span>
                        <xsl:if test="$q">
                            <span>&#160;|&#160;</span><a href="javascript:sortByRank();"><xsl:value-of select="$bundle/value[@key='results.sortby.relevance']"/></a>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <a href="javascript:sortByTitle('asc');"><xsl:value-of select="$bundle/value[@key='results.sortby.name']"/></a>&#160;
                        <xsl:if test="$q">
                            <span>&#160;|&#160;</span><span style="font-weight:bolder;"><xsl:value-of select="$bundle/value[@key='results.sortby.relevance']"/></span>
                        </xsl:if>
                        
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td align="right" style="width:180px;text-align:right;padding-right:3px;">
                <xsl:choose>
                    <xsl:when test="$cols='2'">
                        <a style="float:none;" id="cols1" class="cols" href="javascript:toggleColumns(true);"><xsl:value-of select="$bundle/value[@key='results.1column']"/></a>
                        <a id="cols2" class="cols" href="javascript:toggleColumns(true);" style="float:none;display:none;"><xsl:value-of select="$bundle/value[@key='results.2column']"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <a id="cols1" class="cols" href="javascript:toggleColumns(true);" style="float:none;display:none;"><xsl:value-of select="$bundle/value[@key='results.1column']"/></a>
                        <a style="float:none;" id="cols2" class="cols" href="javascript:toggleColumns(true);"><xsl:value-of select="$bundle/value[@key='results.2column']"/></a>
                    </xsl:otherwise>
                </xsl:choose>                            
            </td>
            </tr>
            </table>
        </div>
    </xsl:template>

</xsl:stylesheet>
