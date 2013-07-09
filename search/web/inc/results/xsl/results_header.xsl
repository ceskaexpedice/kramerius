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
            <div style="float:left;margin-left:5px;">
                <span><xsl:value-of select="$numDocs" />&#160;<xsl:value-of select="$numDocsStr" /></span>
            </div>
            <div style="float:left;margin-left:30px;width:260px;text-align:center;">
                <span><xsl:value-of select="$bundle/value[@key='results.sortby']"/>:</span>&#160;&#160;
                <xsl:choose>
                    <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='sort']='level asc, title_sort asc'">
                        <a href="javascript:sortByTitle('desc');"  style="font-weight:bolder;"><xsl:value-of select="$bundle/value[@key='results.sortby.name']"/></a>&#160;
                        <span class="ui-icon ui-icon-triangle-1-n"  >asc</span>
                        <span>&#160;|&#160;</span><a href="javascript:sortByRank();"><xsl:value-of select="$bundle/value[@key='results.sortby.relevance']"/></a>
                    </xsl:when>
                    <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='sort']='level asc, title_sort desc'">
                        <a href="javascript:sortByTitle('asc');"  style="font-weight:bolder;"><xsl:value-of select="$bundle/value[@key='results.sortby.name']"/></a>&#160;
                        <span class="ui-icon ui-icon-triangle-1-s"  >desc</span>
                        <span>&#160;|&#160;</span><a href="javascript:sortByRank();"><xsl:value-of select="$bundle/value[@key='results.sortby.relevance']"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <a href="javascript:sortByTitle('asc');"><xsl:value-of select="$bundle/value[@key='results.sortby.name']"/></a>&#160;
                        <span>&#160;|&#160;</span><span style="font-weight:bolder;"><xsl:value-of select="$bundle/value[@key='results.sortby.relevance']"/></span>
                    </xsl:otherwise>
                </xsl:choose>
            </div>
            <div style="float:right;margin-right:30px;">
                <xsl:choose>
                    <xsl:when test="$cols='2'">
                        <a id="cols1" class="cols" href="javascript:toggleColumns(true);"><xsl:value-of select="$bundle/value[@key='results.1column']"/></a>
                        <a id="cols2" class="cols" href="javascript:toggleColumns(true);" style="display:none;"><xsl:value-of select="$bundle/value[@key='results.2column']"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <a id="cols1" class="cols" href="javascript:toggleColumns(true);" style="display:none;"><xsl:value-of select="$bundle/value[@key='results.1column']"/></a>
                        <a id="cols2" class="cols" href="javascript:toggleColumns(true);"><xsl:value-of select="$bundle/value[@key='results.2column']"/></a>
                    </xsl:otherwise>
                </xsl:choose>                            
            </div>
        </div>
    </xsl:template>

</xsl:stylesheet>
