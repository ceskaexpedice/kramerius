<xsl:stylesheet  version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="pid" select="pid"/>
    <xsl:param name="level" select="level"/>
    <xsl:param name="onlyrels" select="onlyrels"/>
    <xsl:param name="onlyinfo" select="onlyinfo"/>
    <xsl:key name="keyModel" match="doc" use="str[@name='fedora.model']" />
    <xsl:template match="/">
        <xsl:if test="//doc" >
            <div style="display:none;" class="numDocs"><xsl:value-of select="/response/result/@numFound" /></div>
            <xsl:choose>
            <xsl:when test="$onlyrels='true'">
                <xsl:for-each select="//doc[generate-id(.) = generate-id(key('keyModel', str[@name='fedora.model'])[1])]">
                    <xsl:variable name="lngModelName"><xsl:value-of select="str[@name='fedora.model']" /></xsl:variable>
                    <!-- Select all the Employees belonging to the Team -->
                    <xsl:variable name="lstModel" select="//doc[str[@name='fedora.model']=$lngModelName]" />
                    <!-- Show details for Employees in Team -->
                    <xsl:variable name="model" ><xsl:value-of select="$lstModel[1]/str[@name='fedora.model']" /></xsl:variable>
                    <xsl:call-template name="rels">
                        <xsl:with-param name="fmodel"><xsl:value-of select="$model" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="$onlyinfo='true'">
                <xsl:for-each select="//doc[generate-id(.) = generate-id(key('keyModel', str[@name='fedora.model'])[1])]">
                    <xsl:variable name="lngModelName"><xsl:value-of select="str[@name='fedora.model']" /></xsl:variable>
                    <!-- Select all the Employees belonging to the Team -->
                    <xsl:variable name="lstModel" select="//doc[str[@name='fedora.model']=$lngModelName]" />
                    <!-- Show details for Employees in Team -->
                    <xsl:variable name="model" ><xsl:value-of select="$lstModel[1]/str[@name='fedora.model']" /></xsl:variable>
                    <xsl:call-template name="details">
                        <xsl:with-param name="fmodel"><xsl:value-of select="$model" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="tabs" />
            </xsl:otherwise>
            </xsl:choose>

         </xsl:if>
    </xsl:template>

    <xsl:template name="tabs">
        <div style="padding: 2px;">
        <xsl:attribute name="id">tabs_<xsl:value-of select="$level" /></xsl:attribute>
        <xsl:attribute name="pid"><xsl:value-of select="//doc[position()=1]/str[@name='PID']" /></xsl:attribute>
            <xsl:call-template name="ul" />
            <xsl:for-each select="//doc[generate-id(.) = generate-id(key('keyModel', str[@name='fedora.model'])[1])]">
                <xsl:variable name="lngModelName"><xsl:value-of select="str[@name='fedora.model']" /></xsl:variable>
                <!-- Select all the Employees belonging to the Team -->
                <xsl:variable name="lstModel" select="//doc[str[@name='fedora.model']=$lngModelName]" />
                <!-- Show details for Employees in Team -->
                <xsl:variable name="model" ><xsl:value-of select="$lstModel[1]/str[@name='fedora.model']" /></xsl:variable>
                <xsl:call-template name="model">
                    <xsl:with-param name="fmodel"><xsl:value-of select="$model" /></xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
         </div>
    </xsl:template>

    <xsl:template name="ul">
        <ul>
            <xsl:for-each select="//doc[generate-id(.) = generate-id(key('keyModel', str[@name='fedora.model'])[1])]">
                <li><xsl:attribute name="class"><xsl:value-of select="./str[@name='fedora.model']" />
                    </xsl:attribute>
                    <a>
                    <xsl:attribute name="href">#tab<xsl:value-of select="$level" />-<xsl:value-of select="./str[@name='fedora.model']" />
                </xsl:attribute><span class="translate"><xsl:value-of select="./str[@name='fedora.model']" /></span></a>
                <img width="12px" src="img/empty.gif" class="op_list" alt="list" >
                <xsl:attribute name="onclick">toggleRelsList(this, '<xsl:value-of select="./str[@name='fedora.model']" />')</xsl:attribute></img>
                    <img width="12" src="img/lupa.png" class="searchInsideButton" alt="search" >
                        <xsl:attribute name="onclick">showSearchInside(<xsl:value-of select="$level" />, '<xsl:value-of select="./str[@name='fedora.model']" />')</xsl:attribute>
                    </img>
                </li>
            </xsl:for-each>
            </ul>
    </xsl:template>

    <xsl:template name="info">

    </xsl:template>

    <xsl:template name="rels">
        <xsl:param name="fmodel" />
        <xsl:for-each select="//doc[str[@name='fedora.model']=$fmodel]" >
            <div onclick="selectRelItem(this);">
                <xsl:attribute name="class">relItem <xsl:value-of select="$fmodel" /> <xsl:if test="./str[@name='page_format']" > viewable</xsl:if></xsl:attribute>
                <xsl:attribute name="pid"><xsl:value-of select="./str[@name='PID']" /></xsl:attribute>
                <xsl:call-template name="details">
                    <xsl:with-param name="fmodel"><xsl:value-of select="$fmodel" /></xsl:with-param>
                </xsl:call-template>
            </div>
        </xsl:for-each>

    </xsl:template>

    <xsl:template name="model" >
        <xsl:param name="fmodel" />
        <div><xsl:attribute name="id">tab<xsl:value-of select="$level" />-<xsl:value-of select="$fmodel" /></xsl:attribute>
        <div class="relList" style="display:none;"><xsl:attribute name="id">list-<xsl:value-of select="$fmodel" /></xsl:attribute>
        <xsl:call-template name="rels">
            <xsl:with-param name="fmodel"><xsl:value-of select="$fmodel" /></xsl:with-param>
        </xsl:call-template>
        </div>
        <div><xsl:attribute name="id">info-<xsl:value-of select="$fmodel" />
            </xsl:attribute>
            <xsl:call-template name="details">
                    <xsl:with-param name="fmodel"><xsl:value-of select="$fmodel" /></xsl:with-param>
            </xsl:call-template>
        </div>
        </div>
    </xsl:template>

    <xsl:template name="details">
        <xsl:param name="fmodel" />

            <xsl:choose>
                <xsl:when test="$fmodel='monograph'">
                    <xsl:value-of select="./str[@name='dc.title']" /><br />
                    <xsl:value-of select="$bundle/value[@key='author']"/>: <xsl:value-of select="./arr[@name='dc.creator']/str" />
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
                    <xsl:value-of select="./str[@name='dc.title']" />&#160;
                    <span class="translate"><xsl:value-of select="./arr[@name='details']/str" /></span>&#160;
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
        <xsl:if test="substring-before($detail, '##')!=''">
        <xsl:value-of select="substring-before($detail, '##')" /><br/>
        </xsl:if>
        <xsl:variable name="remaining" select="substring-after($detail, '##')" />
        <xsl:if test="substring-before($remaining, '##')!=''">
        <xsl:value-of select="substring-before($remaining, '##')" /><br/>
        </xsl:if>
        <xsl:variable name="remaining" select="substring-after($remaining, '##')" />
        <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>:
        <xsl:value-of select="substring-before($remaining, '##')" />&#160;
        <xsl:value-of select="$bundle/value[@key='Číslo']"/>
        <xsl:value-of select="substring-after($remaining, '##')" />


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
