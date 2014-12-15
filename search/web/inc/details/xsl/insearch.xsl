<xsl:stylesheet  version="1.0"
    xmlns:exts="java://cz.incad.utils.XslHelper"
    xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="exts java"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:variable name="generic" select="exts:new()" />
    
    <xsl:template match="/">
            <xsl:variable name="rows"><xsl:value-of select="number(/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='rows'])" /></xsl:variable>
            <xsl:variable name="start">
                <xsl:choose>
                    <xsl:when test="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start']/text()">
                        <xsl:value-of select="number(/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='start'])" />
                    </xsl:when>
                    <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>

            </xsl:variable>
            <xsl:variable name="numDocs"><xsl:value-of select="number(/response/result/@numFound)" /></xsl:variable>

        <xsl:variable name="pag">
        <xsl:call-template name="pagination">
            <xsl:with-param name="rows"><xsl:value-of select="$rows" /></xsl:with-param>
            <xsl:with-param name="start"><xsl:value-of select="$start" /></xsl:with-param>
            <xsl:with-param name="numDocs"><xsl:value-of select="$numDocs" /></xsl:with-param>
        </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="numDocsStr">
        <xsl:call-template name="numDocs">
            <xsl:with-param name="numDocs"><xsl:value-of select="$numDocs" /></xsl:with-param>
        </xsl:call-template>
        </xsl:variable>
        
        <div class="numDocs">
        <xsl:copy-of select="$numDocsStr" />
        <xsl:if test="$numDocs &gt; 0"><xsl:copy-of select="$pag" /></xsl:if>
        </div>
        
        <div style="float:left;width:100%">
        <xsl:if test="//doc" >
            <table width="100%">
                <xsl:for-each select="//doc" >
                    <xsl:variable name="pid" ><xsl:value-of select="./str[@name='PID']" /></xsl:variable>
                    <tr>
                        <xsl:attribute name="id">res_<xsl:value-of select="$pid"/></xsl:attribute>
                        <xsl:attribute name="class">result r<xsl:value-of select="position() mod 2"/></xsl:attribute>
                        <xsl:call-template name="doc">
                            <xsl:with-param name="pos"><xsl:value-of select="position()" /></xsl:with-param>
                            <xsl:with-param name="pid"><xsl:value-of select="$pid" /></xsl:with-param>
                        </xsl:call-template>
                    </tr>
                </xsl:for-each>
            </table>
         </xsl:if>
         </div>
         <div class="numDocs">
        <xsl:if test="$numDocs &gt; 0"><xsl:copy-of select="$pag" /></xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="numDocs">
        <xsl:param name="numDocs"/>
        <xsl:value-of select="$numDocs" />&#160;
            <xsl:choose>
                <xsl:when test="$numDocs = 1"><xsl:value-of select="$bundle/value[@key='common.documents.singular']"/></xsl:when>
                <xsl:when test="$numDocs &gt; 1 and $numDocs &lt; 5"><xsl:value-of select="$bundle/value[@key='common.documents.plural_1']"/></xsl:when>
                <xsl:when test="$numDocs &gt; 4"><xsl:value-of select="$bundle/value[@key='common.documents.plural_2']"/></xsl:when>
            </xsl:choose>
    </xsl:template>
    
    <xsl:template name="pagination">
        <xsl:param name="rows"/>
        <xsl:param name="start"/>
        <xsl:param name="numDocs"/>
        <xsl:if test="$numDocs &gt; 0">
            <div style="float:right;">
            <xsl:if test="$start &gt; 0">
                <a class="previous"><xsl:attribute name="href">javascript:searchInside(<xsl:value-of select="$start - $rows" />)</xsl:attribute> &lt;&lt;</a>
            </xsl:if>
            <span>&#160;<xsl:value-of select="1 + $start" /> -
            <xsl:choose>
            <xsl:when test="$numDocs &gt; $rows + $start"><xsl:value-of select="$rows + $start" /></xsl:when>
            <xsl:otherwise><xsl:value-of select="$numDocs" /></xsl:otherwise></xsl:choose>
            &#160;</span>
            <xsl:if test="$numDocs &gt; $rows + $start">
                <a class="next"><xsl:attribute name="href">javascript:searchInside(<xsl:value-of select="$rows + $start" />)</xsl:attribute> &gt;&gt;</a>
            </xsl:if>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="doc">
        <xsl:param name="pid" />
        <xsl:param name="pos" />
        <xsl:variable name="fmodel" ><xsl:value-of select="./str[@name='fedora.model']" /></xsl:variable>
        <xsl:variable name="root_pid" ><xsl:value-of select="./str[@name='root_pid']" /></xsl:variable>
        <xsl:variable name="imagepid" >
            <xsl:choose>
                <xsl:when test="contains($pid, '/@')">img?uuid=<xsl:value-of select="substring-before($pid, '/@')"/>&amp;stream=IMG_THUMB&amp;action=SCALE&amp;scaledHeight=96</xsl:when>
                <xsl:otherwise>img?uuid=<xsl:value-of select="$pid" />&amp;stream=IMG_THUMB&amp;action=SCALE&amp;scaledHeight=96</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <td class="resultThumb" valign="top">
            <a>
                <xsl:attribute name="href">javascript:changeSelect('<xsl:value-of select="./arr[@name='pid_path']/str"/>');</xsl:attribute>
            <img border="1"><xsl:attribute name="id">img_<xsl:value-of select="$pid"/></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="$imagepid" /></xsl:attribute></img></a>
        </td>
        <xsl:variable name="model"><xsl:value-of select="concat('fedora.model.', ./str[@name='fedora.model'])" /></xsl:variable>
        <td class="resultText">
            <a><xsl:attribute name="href">javascript:changeSelect('<xsl:value-of select="./arr[@name='pid_path']/str"/>');</xsl:attribute><b><xsl:value-of select="./str[@name='dc.title']"/></b></a>&#160;
            (<xsl:value-of select="$bundle/value[@key=$model]"/>)
            <div>
            <xsl:call-template name="default">
                <xsl:with-param name="fmodel"><xsl:value-of select="$fmodel" /></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="teaser">
                <xsl:with-param name="pid"><xsl:value-of select="$pid" /></xsl:with-param>
            </xsl:call-template>
            <div class="extInfo" style="display:none;"><xsl:value-of select="./arr[@name='pid_path']/str[position()=1]"/></div>
            </div>
        </td>
    </xsl:template>

    <xsl:template name="default" >
        <xsl:if test="./arr[@name='dc.creator']/str">
        </xsl:if>
        <xsl:if test="./str[@name='datum']">
        <xsl:value-of select="./str[@name='datum']" /><br/>
        </xsl:if>

    </xsl:template>

    <xsl:template name="teaser">
        <xsl:param name="pid" />
        <div class="teaser">
        <xsl:for-each select="../../lst[@name='highlighting']/lst">
            <xsl:if test="@name = $pid">
                <xsl:for-each select="./arr[@name='text']/str">
                (... <xsl:value-of select="."   disable-output-escaping="yes" /> ...)<br/>
                </xsl:for-each>
            </xsl:if>
        </xsl:for-each>
        </div>
    <br/>


    </xsl:template>

    <xsl:template name="details">
        <xsl:param name="fmodel" />
        <xsl:for-each select="./arr[@name='details']/str">
            <xsl:choose>
                <xsl:when test="$fmodel='periodicalvolume'">
                    <xsl:call-template name="periodicalvolume">
                        <xsl:with-param name="detail"><xsl:value-of select="." /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$fmodel='monographunit'">
                    <xsl:value-of select="../../str[@name='dc.title']" /><br/>
                    <xsl:call-template name="monographunit">
                        <xsl:with-param name="detail"><xsl:value-of select="." /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$fmodel='internalpart'">
                    <xsl:value-of select="dc.title" />&#160;
                    <xsl:call-template name="internalpart">
                        <xsl:with-param name="detail"><xsl:value-of select="." /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$fmodel='page'">
                    <xsl:call-template name="page">
                        <xsl:with-param name="detail"><xsl:value-of select="." /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <span class="translate"><xsl:value-of select="." /></span>&#160;
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>

    </xsl:template>

    <xsl:template name="periodicalvolume">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>:
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <xsl:value-of select="$bundle/value[@key='Číslo']"/>
        <xsl:value-of select="substring-after($detail, '##')" />
    </xsl:template>


    <xsl:template name="monographunit">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key='Volume']"/>:&#160;<xsl:value-of select="substring-before($detail, '##')" />&#160;
        <span class="translate"><xsl:value-of select="substring-after($detail, '##')" /></span>
    </xsl:template>

    <xsl:template name="internalpart">
        <xsl:param name="detail" />
        <span class="translate"><xsl:value-of select="substring-before($detail, '##')" /></span>:&#160;
        <xsl:variable name="remaining" select="substring-after($detail, '##')" />
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <xsl:variable name="remaining" select="substring-after($remaining, '##')" />
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <xsl:value-of select="substring-after($remaining, '##')" />
    </xsl:template>

    <xsl:template name="page">
        <xsl:param name="detail" />
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <span class="translate"><xsl:value-of select="substring-after($detail, '##')" /></span>
    </xsl:template>


</xsl:stylesheet>
