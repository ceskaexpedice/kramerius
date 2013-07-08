<xsl:stylesheet  version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="pid" select="pid"/>
    <xsl:param name="model_path" select="model_path"/>
    <xsl:param name="onlyrels" select="onlyrels"/>
    <xsl:param name="onlyinfo" select="onlyinfo"/>
    <xsl:param name="policyPublic" select="policyPublic"/>
    <xsl:key name="keyModel" match="doc" use="str[@name='fedora.model']" />
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$onlyrels='true'">
                <xsl:if test="//doc" >
                    <xsl:for-each select="//doc[generate-id(.) = generate-id(key('keyModel', str[@name='fedora.model'])[1])]">
                        <xsl:variable name="lngModelName">
                            <xsl:value-of select="str[@name='fedora.model']" />
                        </xsl:variable>
                        <xsl:variable name="lstModel" select="//doc[str[@name='fedora.model']=$lngModelName]" />
                        <xsl:variable name="model" >
                            <xsl:value-of select="$lstModel[1]/str[@name='fedora.model']" />
                        </xsl:variable>
                        <xsl:call-template name="rels">
                            <xsl:with-param name="fmodel">
                                <xsl:value-of select="$model" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:if>
            </xsl:when>
            <xsl:when test="$onlyinfo='true'">
                <xsl:if test="//doc" >
                    <xsl:for-each select="//doc[generate-id(.) = generate-id(key('keyModel', str[@name='fedora.model'])[1])]">
                        <xsl:variable name="lngModelName"><xsl:value-of select="str[@name='fedora.model']" /></xsl:variable>
                        <xsl:variable name="lstModel" select="//doc[str[@name='fedora.model']=$lngModelName]" />
                        <xsl:variable name="model" ><xsl:value-of select="$lstModel[1]/str[@name='fedora.model']" /></xsl:variable>
                        <xsl:call-template name="details">
                            <xsl:with-param name="fmodel"><xsl:value-of select="$model" /></xsl:with-param>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="tabs" />
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template name="tabs">
        <ul>
            <xsl:if test="//doc" >
                <xsl:for-each select="//doc[generate-id(.) = generate-id(key('keyModel', str[@name='fedora.model'])[1])]">
                    <xsl:variable name="lngModelName">
                        <xsl:value-of select="str[@name='fedora.model']" />
                    </xsl:variable>
                    <xsl:variable name="lstModel" select="//doc[str[@name='fedora.model']=$lngModelName]" />
                    <xsl:variable name="model" >
                        <xsl:value-of select="$lstModel[1]/str[@name='fedora.model']" />
                    </xsl:variable>
                <xsl:variable name="modelLoc" ><xsl:choose>
                <xsl:when test="$model='periodicalvolume'">search.details.periodicalvolume.in.tree</xsl:when>
                <xsl:otherwise>fedora.model.<xsl:value-of select="$model" /></xsl:otherwise>
            </xsl:choose></xsl:variable>
                    <li class="model" style="clear:both;">
                    <xsl:attribute name="id"><xsl:value-of select="$model_path" />-<xsl:value-of select="$model" />_<xsl:value-of select="$pid" /></xsl:attribute>
                        <span class="ui-icon ui-icon-triangle-1-e folder" ></span>
                    
                        <a>
                        <xsl:attribute name="href">i.jsp?pid=<xsl:value-of select="$pid" /></xsl:attribute>
                            <xsl:value-of select="$bundle/value[@key=$modelLoc]"/>
                        </a>
                        <xsl:call-template name="model">
                            <xsl:with-param name="fmodel">
                                <xsl:value-of select="$model" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </li>
                </xsl:for-each>
            </xsl:if>
        </ul>
    </xsl:template>

    <xsl:template name="rels">
        <xsl:param name="fmodel" />
        <xsl:variable name="modelLoc" ><xsl:choose>
                <xsl:when test="$fmodel='periodicalvolume'">search.details.periodicalvolume.in.tree</xsl:when>
                <xsl:otherwise>fedora.model.<xsl:value-of select="$fmodel" /></xsl:otherwise>
            </xsl:choose></xsl:variable>
        <xsl:for-each select="//doc[str[@name='fedora.model']=$fmodel]" >
            <xsl:sort data-type="number" 
                      select="number(arr[@name='rels_ext_index']/int[position()=(count(../../arr[@name='parent_pid']/str[.=$pid]/preceding-sibling::*)+1)])" 
                      order="ascending" /> 
            <xsl:variable name="posss"><xsl:value-of select="count(arr[@name='parent_pid']/str[.=$pid]/preceding-sibling::*)+1" /></xsl:variable>
            <xsl:variable name="idx"><xsl:value-of select="number(arr[@name='rels_ext_index']/int[position()=$posss])" /></xsl:variable>
            <li style="clear:both;">
                <xsl:attribute name="id"><xsl:value-of select="$model_path" />-<xsl:value-of select="$fmodel" />_<xsl:value-of select="./str[@name='PID']" /></xsl:attribute>
                <xsl:attribute name="class"><xsl:value-of select="./str[@name='dostupnost']" /><xsl:if test="./bool[@name='viewable']" > viewable</xsl:if></xsl:attribute>
                <span class="ui-icon ui-icon-triangle-1-e folder" >folder</span>
                <div style="float:left;"><input type="checkbox"  /></div>
                <div style="float:left;"><a>
                    <xsl:attribute name="href">i.jsp?pid=<xsl:value-of select="./str[@name='PID']" /></xsl:attribute>
                        <xsl:call-template name="details">
                            <xsl:with-param name="fmodel">
                                <xsl:value-of select="$fmodel" />
                            </xsl:with-param>
                        </xsl:call-template>
                </a></div>
            </li>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="model" >
        <xsl:param name="fmodel" />
        <ul style="display:block;">
            <xsl:attribute name="class">
                <xsl:value-of select="$fmodel" />
            </xsl:attribute>
            <xsl:call-template name="rels">
                <xsl:with-param name="fmodel">
                    <xsl:value-of select="$fmodel" />
                </xsl:with-param>
            </xsl:call-template>
        </ul>
    </xsl:template>

    <xsl:template name="details">
        <xsl:param name="fmodel" />
        <xsl:if test="./str[@name='img_full_mime']" >
            <span>
                <xsl:attribute name="style">text-indent:-200px;background: url('img/mime/<xsl:value-of select="./str[@name='img_full_mime']" />.png'); width:16px;height:16px;float:left;</xsl:attribute>
                <xsl:attribute name="title"><xsl:value-of select="./str[@name='img_full_mime']" /></xsl:attribute>
            mime</span>
        </xsl:if>
        <xsl:if test="not($policyPublic='true')  and not(./str[@name='dostupnost']='public')" >
            <span>
                <xsl:attribute name="style">text-indent:-200px;background: url('img/lock.png'); width:16px;height:16px;float:left;</xsl:attribute>
                <xsl:attribute name="title"><xsl:value-of select="./str[@name='dostupnost']" /></xsl:attribute>
            private</span>
        </xsl:if>
        <xsl:if test="$policyPublic='true' and ./str[@name='dostupnost']='public'" >
            <span>
                <xsl:attribute name="style">text-indent:-200px;background: url('img/public.png'); width:16px;height:16px;float:left;</xsl:attribute>
                <xsl:attribute name="title"><xsl:value-of select="./str[@name='dostupnost']" /></xsl:attribute>
            public</span>
        </xsl:if>
        <label>
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
                <xsl:when test="$fmodel='track'">
                    <xsl:value-of select="./str[@name='dc.title']" />&#160;
                </xsl:when>
                <xsl:when test="$fmodel='soundunit'">
                    <xsl:value-of select="./str[@name='dc.title']" />&#160;
                </xsl:when>
                <xsl:when test="$fmodel='soundrecording'">
                    soundrecording: 
                    <xsl:value-of select="./str[@name='dc.title']" />&#160;
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="./str[@name='dc.title']" />&#160;
                    <xsl:value-of select="./arr[@name='details']/str" />&#160;
                </xsl:otherwise>
            </xsl:choose>
        </label>
    </xsl:template>

    <xsl:template name="periodicalvolume">
        <xsl:param name="detail" />
        <xsl:choose>
            <xsl:when test="not(string(number(node())) = 'NaN')">
                <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>:
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <xsl:if test="substring-after($detail, '##')">
            <xsl:value-of select="$bundle/value[@key='search.details.periodicalvolumenumber.in.tree']"/>&#160;<xsl:value-of select="substring-after($detail, '##')" />
        </xsl:if>
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
        <span><xsl:value-of select="substring-before($remaining, '##')" />&#160;
              <xsl:value-of select="$bundle/value[@key='Číslo']"/>&#160;<xsl:value-of select="substring-after($remaining, '##')" /></span>
    </xsl:template>

    <xsl:template name="monographunit">
        <xsl:param name="detail" />
        <xsl:value-of select="$bundle/value[@key='Volume']"/>:&#160;
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <xsl:value-of select="substring-after($detail, '##')" />
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
        <xsl:value-of select="substring-before($detail, '##')" />&#160;
        <xsl:variable name="partType">mods.page.partType.<xsl:value-of select="substring-after($detail, '##')"/></xsl:variable>
        <xsl:value-of select="$bundle/value[@key=$partType]"/>
    </xsl:template>

</xsl:stylesheet>
