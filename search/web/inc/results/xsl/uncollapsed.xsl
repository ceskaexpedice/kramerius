<xsl:stylesheet  version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="root_pid" select="root_pid"/>
    <xsl:param name="q" select="q"/>
    <xsl:template match="/">
        <xsl:if test="//doc" >
            <div><xsl:attribute name="id">uncoll_<xsl:value-of select="$root_pid"/></xsl:attribute>
                <xsl:for-each select="//doc" >
                    <xsl:call-template name="rels">
                        <xsl:with-param name="fmodel"><xsl:value-of select="./str[@name='fedora.model']" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:for-each>
            </div>
         </xsl:if>
    </xsl:template>

    <xsl:template name="rels">
        <xsl:param name="fmodel" />
            <xsl:variable name="solruuid"><xsl:value-of select="./str[@name='PID']"/></xsl:variable>
            <div>
                <xsl:attribute name="class">r<xsl:value-of select="position() mod 2"/></xsl:attribute>
                <a><xsl:attribute name="href">i.jsp?pid=<xsl:value-of select="./str[@name='PID']"/>&amp;q=<xsl:value-of select="$q"/>
                </xsl:attribute>
                <xsl:call-template name="details">
                    <xsl:with-param name="fmodel"><xsl:value-of select="$fmodel" /></xsl:with-param>
                </xsl:call-template>
                </a>
                <xsl:if test="./str[@name='datum_str']/text()">
                    <div><xsl:value-of select="./str[@name='datum_str']" /></div>
                </xsl:if>
            <div class="teaser">
                <xsl:for-each select="../../lst[@name='highlighting']/lst">
                    <xsl:if test="@name = $solruuid">
                        <xsl:for-each select="./arr[@name='text_ocr']/str">
                        (... <xsl:value-of select="." disable-output-escaping="yes" /> ...)<br/>
                        </xsl:for-each>
                    </xsl:if>
                </xsl:for-each>
            </div>
            </div>

    </xsl:template>


    <xsl:template name="details">
        <xsl:param name="fmodel" />
            <xsl:choose>
                <xsl:when test="$fmodel='monograph'">
                    <xsl:value-of select="./str[@name='dc.title']" />&#160;
                </xsl:when>
                <xsl:when test="$fmodel='monographunit'">
                    <xsl:value-of select="$bundle/value[@key='fedora.model.monographunit']"/>:&#160;
                    <xsl:value-of select="./str[@name='dc.title']" />&#160;
                    <xsl:call-template name="monographunit">
                        <xsl:with-param name="detail"><xsl:value-of select="./arr[@name='details']/str" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$fmodel='periodical'">
                    <xsl:value-of select="./str[@name='dc.title']" />
                </xsl:when>
                <xsl:when test="$fmodel='periodicalvolume'">
                    <xsl:call-template name="periodicalvolume">
                        <xsl:with-param name="detail"><xsl:value-of select="./arr[@name='details']/str" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$fmodel='periodicalitem'">
                    <xsl:call-template name="periodicalitem">
                        <xsl:with-param name="detail"><xsl:value-of select="./arr[@name='details']/str" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$fmodel='internalpart'">
                    <xsl:value-of select="$bundle/value[@key='fedora.model.internalpart']"/>:&#160;
                    <xsl:value-of select="dc.title" />&#160;
                    <xsl:call-template name="internalpart">
                        <xsl:with-param name="detail"><xsl:value-of select="./arr[@name='details']/str" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$fmodel='page'">
                    <xsl:call-template name="page">
                        <xsl:with-param name="detail"><xsl:value-of select="./arr[@name='details']/str" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$bundle/value[@key=./arr[@name='details']/str]"/>&#160;
                </xsl:otherwise>
            </xsl:choose>
    </xsl:template>

    <xsl:template name="periodicalvolume">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>:
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <xsl:value-of select="$bundle/value[@key='Číslo']"/>
        <xsl:value-of select="substring-after($detail, '##')" />
    </xsl:template>

    <xsl:template name="periodicalitem">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key='fedora.model.periodicalitem']"/>&#160;
        <xsl:if test="substring-before($detail, '##')"><xsl:value-of select="substring-before($detail, '##')" /><br/></xsl:if>
        <xsl:variable name="remaining" select="substring-after($detail, '##')" />
        <xsl:value-of select="substring-before($remaining, '##')" /><br/>
        <xsl:variable name="remaining" select="substring-after($remaining, '##')" />
        <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>:&#160;
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <xsl:value-of select="$bundle/value[@key='Číslo']"/>&#160;
        <xsl:value-of select="substring-after($remaining, '##')" />
    </xsl:template>

    <xsl:template name="monographunit">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key=substring-before($detail, '##')]"/>&#160;
        <xsl:value-of select="$bundle/value[@key=substring-after($detail, '##')]"/>
    </xsl:template>

    <xsl:template name="internalpart">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key=substring-before($detail, '##')]"/>:&#160;
        <xsl:variable name="remaining" select="substring-after($detail, '##')" />
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <xsl:variable name="remaining" select="substring-after($remaining, '##')" />
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <xsl:value-of select="substring-after($remaining, '##')" />
    </xsl:template>

    <xsl:template name="page">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key='fedora.model.page']"/>&#160;
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <xsl:value-of select="$bundle/value[@key=substring-after($detail, '##')]"/>
    </xsl:template>

</xsl:stylesheet>
