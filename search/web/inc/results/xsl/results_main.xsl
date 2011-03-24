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

        
        <xsl:if test="/response/result/doc" >
            <table id="results_main" cellspacing="0" cellpadding="0" border="0">
                <xsl:for-each select="/response/result/doc" >
                    <xsl:variable name="pid" ><xsl:value-of select="./str[@name='PID']" /></xsl:variable>
                    <tr>
                        <xsl:attribute name="id">res_<xsl:value-of select="$pid"/></xsl:attribute>
                        <xsl:attribute name="class">result r<xsl:value-of select="position() mod 2"/></xsl:attribute>
                        <xsl:call-template name="doc">
                            <xsl:with-param name="pos"><xsl:value-of select="position()" /></xsl:with-param>
                            <xsl:with-param name="pid"><xsl:value-of select="$pid" /></xsl:with-param>
                        </xsl:call-template>
                    </tr>
                    <tr>
                        <xsl:attribute name="class">uncollapsed r<xsl:value-of select="position() mod 2"/></xsl:attribute>
                        <td></td><td></td>
                        <td><xsl:attribute name="id">uncollapsed_<xsl:value-of select="./str[@name='root_pid']"/></xsl:attribute></td>
                    </tr>
                </xsl:for-each>
            </table>
         </xsl:if>
    </xsl:template>

    <xsl:template name="collapse">
        <xsl:param name="pid" />
        <xsl:param name="root_pid" />
        <xsl:for-each select="/response/lst[@name='collapse_counts']/lst[@name='results']/lst">
                <xsl:if test="./@name=$pid">
                    <xsl:variable name="collapseText" ><xsl:value-of select="./int[@name='collapseCount']/text()"/>&#160;<xsl:value-of select="$bundle/value[@key='collapsed']"/></xsl:variable>
                    <xsl:variable name="collapseCount" >
                        (<a><xsl:attribute name="href">javascript:toggleCollapsed('<xsl:value-of select="$root_pid" />', 'uncollapsed_<xsl:value-of select="$root_pid"/>', 0);</xsl:attribute>
                        <xsl:value-of select="$collapseText"/>
                        <img border="0" src="img/empty.gif" class="collapseIcon">
                            <xsl:attribute name="id">uimg_<xsl:value-of select="$root_pid"/></xsl:attribute>
                            <xsl:attribute name="alt"><xsl:value-of select="$collapseText"/></xsl:attribute>
                        </img>
                        </a>)
                    </xsl:variable>
                    <span><xsl:copy-of select="$collapseCount"  /></span>
                </xsl:if>
            </xsl:for-each>
    </xsl:template>

    <xsl:template name="doc">
        <xsl:param name="pid" />
        <xsl:param name="pos" />
        <xsl:variable name="fmodel" ><xsl:value-of select="./str[@name='fedora.model']" /></xsl:variable>
        <xsl:variable name="root_pid" ><xsl:value-of select="./str[@name='root_pid']" /></xsl:variable>
        <xsl:variable name="link" >./item.jsp?pid=<xsl:value-of
        select="$pid"/>&amp;pid_path=<xsl:value-of
        select="./str[@name='pid_path']"/>&amp;path=<xsl:value-of
        select="./str[@name='path']"/>&amp;format=<xsl:value-of
        select="./str[@name='page_format']"/>&amp;q=<xsl:value-of
        select="$q"/><xsl:value-of select="$fqs" disable-output-escaping="yes" />
        </xsl:variable>
        
        <xsl:variable name="imagepid" >
            <xsl:choose>
                <!-- XXX: Changed thumb servlet to img servlet -->
                <xsl:when test="contains($pid, '/@')">img?uuid=<xsl:value-of select="substring-before($pid, '/@')"/>&amp;stream=IMG_THUMB&amp;action=SCALE&amp;scaledHeight=64</xsl:when>
                <xsl:when test="$fmodel='page'">img?uuid=<xsl:value-of select="$pid"/>&amp;stream=IMG_THUMB&amp;action=SCALE&amp;scaledHeight=64</xsl:when>
                <xsl:otherwise>img?uuid=<xsl:value-of select="$pid" />&amp;stream=IMG_THUMB&amp;action=SCALE&amp;scaledHeight=64</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <td style="float:left;">
        <img src="img/empty.gif">
            <xsl:if test="position() > 5" >
                <xsl:attribute name="class">plus</xsl:attribute>
                <xsl:attribute name="onclick">$('#more_<xsl:value-of select="$pid"/>').toggle();$('#img_<xsl:value-of select="$pid"/>').toggle();$(this).toggleClass('minus')</xsl:attribute>
            </xsl:if>
        </img>
        </td>
        <td class="resultThumb" valign="top">
            <a>
                <xsl:attribute name="href"><xsl:value-of select="normalize-space($link)"/></xsl:attribute>
            <img border="1"><xsl:attribute name="id">img_<xsl:value-of select="$pid"/></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="$imagepid" /></xsl:attribute>
            <xsl:if test="position() > 5" >
                <xsl:attribute name="style">display:none;</xsl:attribute>
            </xsl:if></img></a>
        </td>
        <xsl:variable name="model"><xsl:value-of select="concat('fedora.model.', ./str[@name='fedora.model'])" /></xsl:variable>
        <td class="resultText">
            <a><xsl:attribute name="href"><xsl:value-of select="normalize-space($link)"/></xsl:attribute><b><xsl:value-of select="./str[@name='root_title']"/></b></a>&#160;
            (<xsl:value-of select="$bundle/value[@key=$model]"/>)
            <xsl:call-template name="collapse">
                <xsl:with-param name="pid"><xsl:value-of select="$pid" /></xsl:with-param>
                <xsl:with-param name="root_pid"><xsl:value-of select="$root_pid"/></xsl:with-param>
            </xsl:call-template>
            <div><xsl:attribute name="id">more_<xsl:value-of select="$pid"/></xsl:attribute>
            <xsl:if test="position() > 5" >
                <xsl:attribute name="style">display:none;</xsl:attribute>
            </xsl:if>
            <xsl:call-template name="othersfields">
                <xsl:with-param name="fmodel"><xsl:value-of select="$fmodel" /></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="details">
                <xsl:with-param name="fmodel"><xsl:value-of select="$fmodel" /></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="teaser">
                <xsl:with-param name="pid"><xsl:value-of select="$pid" /></xsl:with-param>
            </xsl:call-template>
            </div>
        </td>
    </xsl:template>

    <xsl:template name="othersfields" >
        <xsl:if test="./arr[@name='dc.creator']/str/text()">
            <xsl:for-each select="./arr[@name='dc.creator']/str">
                <xsl:if test="position() > 1">;&#160;</xsl:if><xsl:value-of select="."/>
            </xsl:for-each><br/>
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
