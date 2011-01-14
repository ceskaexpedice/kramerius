<xsl:stylesheet  version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:param name="root_pid" select="root_pid"/>
    <xsl:param name="q" select="q"/>
    <xsl:template match="/">
        <xsl:if test="//doc" >
            <div><xsl:attribute name="id">uncoll_<xsl:value-of select="$root_pid"/></xsl:attribute>
                
                <xsl:for-each select="//doc" >
                    <xsl:if test="not(preceding-sibling::*[1]/str[@name='fedora.model'] = ./str[@name='fedora.model']/text())">
                    <xsl:call-template name="rels">
                        <xsl:with-param name="fmodel"><xsl:value-of select="./str[@name='fedora.model']" /></xsl:with-param>
                    </xsl:call-template>
                    </xsl:if>
                </xsl:for-each>
            </div>
         </xsl:if>
    </xsl:template>

    <xsl:template name="rels">
        <xsl:param name="fmodel" />
        <xsl:for-each select="//doc[str[@name='fedora.model']=$fmodel]" >
            <xsl:variable name="solruuid"><xsl:value-of select="./str[@name='PID']"/></xsl:variable>
            <div style="border-top:solid 1px silver;"><a><xsl:attribute name="href">item.jsp?pid=<xsl:value-of select="./str[@name='PID']"/>&amp;pid_path=<xsl:value-of select="./str[@name='pid_path']"/>&amp;path=<xsl:value-of select="./str[@name='path']"/>&amp;q=<xsl:value-of select="$q"/>
                </xsl:attribute>
                <xsl:call-template name="details">
                    <xsl:with-param name="fmodel"><xsl:value-of select="$fmodel" /></xsl:with-param>
                </xsl:call-template>
            </a>
            <div class="teaser">
                <xsl:for-each select="../../lst[@name='highlighting']/lst">
                    <xsl:if test="@name = $solruuid">
                        <xsl:for-each select="./arr[@name='text']/str">
                        (... <xsl:value-of select="." disable-output-escaping="yes" /> ...)<br/>
                        </xsl:for-each>
                    </xsl:if>
                </xsl:for-each>
            </div>
            </div>
        </xsl:for-each>

    </xsl:template>


    <xsl:template name="details">
        <xsl:param name="fmodel" />

            <xsl:choose>
                <xsl:when test="$fmodel='monograph'">
                    <xsl:value-of select="./str[@name='dc.title']" /><br />
                    autor: <xsl:value-of select="./arr[@name='dc.creator']/str" />
                </xsl:when>
                <xsl:when test="$fmodel='monographunit'">
                    <xsl:value-of select="./str[@name='dc.title']" /><br/>
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
                    <span class="translate"><xsl:value-of select="./arr[@name='details']/str" /></span>&#160;
                </xsl:otherwise>
            </xsl:choose>


    </xsl:template>

    <xsl:template name="periodicalvolume">
        <xsl:param name="detail" />
        <span class="translate">Datum vydání</span>:
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <span class="translate">Číslo</span>
        <xsl:value-of select="substring-after($detail, '##')" />
    </xsl:template>


    <xsl:template name="periodicalitem">
        <xsl:param name="detail" />
        <xsl:value-of select="substring-before($detail, '##')" /><br/>
        <xsl:variable name="remaining" select="substring-after($detail, '##')" />
        <xsl:value-of select="substring-before($remaining, '##')" /><br/>
        <xsl:variable name="remaining" select="substring-after($remaining, '##')" />
        <span class="translate">Datum vydání</span>:
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <span class="translate">Číslo</span>
        <xsl:value-of select="substring-after($remaining, '##')" />


    </xsl:template>

    <xsl:template name="monographunit">
        <xsl:param name="detail" />
        <span class="translate">Volume</span>:&#160;<xsl:value-of select="substring-before($detail, '##')" />&#160;
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
        <span class="translate">fedora.model.page</span>:&#160;
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <span class="translate"><xsl:value-of select="substring-after($detail, '##')" /></span>
    </xsl:template>

</xsl:stylesheet>
