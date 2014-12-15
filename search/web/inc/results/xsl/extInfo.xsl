
<xsl:stylesheet  version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="pid" select="pid"/>
    <xsl:template match="/">
        <div class="ui-tabs-panel ui-widget-content">
            <xsl:for-each select="//doc" >
            <xsl:sort select="position()" data-type="number" order="descending"/>
            <xsl:variable name="model"><xsl:value-of select="concat('fedora.model.', ./str[@name='fedora.model'])" /></xsl:variable>
            <span><xsl:if test="position()&gt;1">| &#160;</xsl:if><xsl:value-of select="$bundle/value[@key=$model]" />: 
            <a><xsl:attribute name="href">i.jsp?pid=<xsl:value-of
        select="./str[@name='PID']"/></xsl:attribute>
            <!-- <xsl:value-of select="./str[@name='dc.title']" /> -->
            <xsl:call-template name="details" />
            &#160;
            </a></span> 
        </xsl:for-each></div>
        <div class="clear"></div>
    </xsl:template>

    <xsl:template name="details">
        <xsl:variable name="fmodel" ><xsl:value-of select="./str[@name='fedora.model']" /></xsl:variable>
        <span>
        <xsl:choose>
            <xsl:when test="$fmodel='monograph'">
                <xsl:value-of select="./str[@name='dc.title']" />
            </xsl:when>
            <xsl:when test="$fmodel='monographunit'">
                <xsl:call-template name="monographunit">
                    <xsl:with-param name="detail">
                        <xsl:value-of select="./arr[@name='details']/str" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$fmodel='periodical'">
                <xsl:value-of select="./str[@name='dc.title']" />
            </xsl:when>
            <xsl:when test="$fmodel='periodicalvolume'">
                <xsl:call-template name="periodicalvolume">
                    <xsl:with-param name="detail">
                        <xsl:value-of select="./arr[@name='details']/str" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$fmodel='periodicalitem'">
                <xsl:call-template name="periodicalitem">
                    <xsl:with-param name="detail">
                        <xsl:value-of select="./arr[@name='details']/str" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$fmodel='internalpart'">
                <xsl:value-of select="dc.title" />&#160;
                <xsl:call-template name="internalpart">
                    <xsl:with-param name="detail">
                        <xsl:value-of select="./arr[@name='details']/str" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$fmodel='page'">
                <xsl:call-template name="page">
                    <xsl:with-param name="detail">
                        <xsl:value-of select="./arr[@name='details']/str" />
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="./str[@name='dc.title']" />&#160;
                    <xsl:value-of select="./arr[@name='details']/str" />&#160;
            </xsl:otherwise>
        </xsl:choose>
        </span>
    </xsl:template>

    <xsl:template name="periodicalvolume">
        <xsl:param name="detail" />
        <xsl:value-of select="substring-after($detail, '##')" />&#160;
        (<xsl:value-of select="substring-before($detail, '##')" />)
    </xsl:template>

    <xsl:template name="periodicalitem">
        <xsl:param name="detail" />
        <xsl:if test="substring-before($detail, '##')!='' and substring-before($detail, '##')!=./str[@name='root_title']">
            <span><xsl:value-of select="substring-before($detail, '##')" />&#160;</span>
        </xsl:if>
        <xsl:variable name="remaining" select="substring-after($detail, '##')" />
        <xsl:if test="substring-before($remaining, '##')!=''">
            <span><xsl:value-of select="substring-before($remaining, '##')" />&#160;</span>
        </xsl:if>
        <xsl:variable name="remaining" select="substring-after($remaining, '##')" />
        <span>
        <xsl:value-of select="substring-after($remaining, '##')" />&#160;
        </span>


    </xsl:template>

    <xsl:template name="monographunit">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key='Volume']"/>:&#160;
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <xsl:value-of select="substring-after($detail, '##')" />
    </xsl:template>

    <xsl:template name="internalpart">
        <xsl:param name="detail" />
        <xsl:if test="substring-before($detail, '##')!=''">
        <xsl:value-of select="$bundle/value[@key=substring-before($detail, '##')]"/>:&#160;
        </xsl:if>
        <xsl:variable name="remaining" select="substring-after($detail, '##')" />
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <xsl:variable name="remaining" select="substring-after($remaining, '##')" />
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <xsl:value-of select="substring-after($remaining, '##')" />
    </xsl:template>

    <xsl:template name="page">
        <xsl:param name="detail" />
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <xsl:value-of select="$bundle/value[@key=substring-after($detail, '##')]"/>
    </xsl:template>

</xsl:stylesheet>
