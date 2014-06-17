<xsl:stylesheet  version="1.0"
    xmlns:exts="java://cz.incad.utils.XslHelper"
    xmlns:java="http://xml.apache.org/xslt/java"
    exclude-result-prefixes="exts java"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="numOpenedRows" select="numOpenedRows" />
    <xsl:param name="policyPublic" select="policyPublic"/>
    <xsl:variable name="generic" select="exts:new()" />
    <xsl:variable name="fqVal"><xsl:value-of select="/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='fq']/text()" /></xsl:variable>
    <xsl:key name="rootModel" match="lst[@name='model_path']/int/@name" use="substring-before(., '/')" />
    <xsl:template match="/">
        <div id="facets" style="font-size:1.2em;">
        <ul>
            <xsl:call-template name="facets" />
        </ul>
        </div>
        
        <script type="text/javascript">
        <xsl:comment><![CDATA[
        $(document).ready(function(){
            //$('#facets>ul>li>ul>li.more_facets').toggle();
            $('#facets>ul>li>a').click(function(event){
                var id = $(this).parent().attr('id');
                toggleFacet(id);
                event.stopPropagation();
                return false;
            });
        });
        function toggleFacet(id){
            $('#'+id+'>ul>li.more_facets').toggle();
            $('#'+id+'>span').toggleClass('ui-icon-triangle-1-s');
        }

    function addFilter(field, value){
        var page = new PageQuery(window.location.search);
        page.setValue("offset", "0");
        page.setValue("forProfile", "facet");
        
        var f = "fq=" + field + ":\"" + value + "\"";
        if(window.location.search.indexOf(f)==-1){
            window.location = "r.jsp?" +
            page.toString() + "&" + f;
        }
    }

    function addTypeFilter(value){
        var page = new PageQuery(window.location.search);
        page.setValue("offset", "0");
        page.setValue("forProfile", "facet");
                
        var f = "fq=model_path:" + value + "*";
        if(window.location.search.indexOf(f)==-1){
            window.location = "r.jsp?" +
            page.toString() + "&" + f;
        }
    }

        ]]></xsl:comment>
        </script>
    </xsl:template>

    <xsl:template name="facets">
        <xsl:for-each select="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst">
            <xsl:variable name="nav" >
                <xsl:copy-of select="."/>
            </xsl:variable>
            <xsl:variable name="navName" >
                <xsl:value-of select="./@name"/>
            </xsl:variable>
            <xsl:if test="count(./int) > 1 and not($navName = 'rok')">
                <xsl:choose>
                    <xsl:when test="./@name = 'model_path'">
                        <xsl:call-template name="model_path">
                            <xsl:with-param name="facetname"><xsl:value-of select="./@name" /></xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="facet">
                            <xsl:with-param name="facetname"><xsl:value-of select="./@name" /></xsl:with-param>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="model_path">
        <xsl:param name="facetname" />
        <xsl:variable name="facetname_bundle">facet.<xsl:value-of select="$facetname" /></xsl:variable>

        <li style="float:left; border:1px solid silver;margin:5px;padding:5px;width:30%;">
            <xsl:attribute name="id">facet_<xsl:value-of select="$facetname"/></xsl:attribute>
            <span class="ui-icon ui-icon-triangle-1-e folder" ></span>
            <a href="#"><xsl:value-of select="$bundle/value[@key=$facetname_bundle]" /></a>

            <ul class="facet"><xsl:for-each select="./int/@name[generate-id(.) = generate-id(key('rootModel', substring-before(., '/'))[1])]">
                <xsl:variable name="u"><xsl:value-of select="substring-before(., '/')" /></xsl:variable>
                <xsl:variable name="f"><xsl:value-of select="concat('fedora.model.', $u)" /></xsl:variable>
                <li><a>
                    <xsl:attribute name="href">javascript:addTypeFilter('<xsl:value-of select="$u" />')</xsl:attribute>
                    <xsl:value-of select="$bundle/value[@key=$f]" />
                </a>
                </li>
            </xsl:for-each></ul>

        </li>
    </xsl:template>
    <xsl:template name="facet">
        <xsl:param name="facetname" />
        <xsl:variable name="facetname_bundle">facet.<xsl:value-of select="$facetname" /></xsl:variable>
        <li style="float:left; border:1px solid silver;margin:5px;padding:5px;width:30%;">
            <xsl:attribute name="id">facet_<xsl:value-of select="$facetname"/></xsl:attribute>
            <span class="ui-icon ui-icon-triangle-1-e folder" ></span>
            <a href="#"><xsl:value-of select="$bundle/value[@key=$facetname_bundle]" /></a>
            <ul><xsl:for-each select="./int">
                <xsl:variable name="fqId"><xsl:value-of select="$facetname" />:"<xsl:value-of select="@name" />"</xsl:variable>
                <xsl:variable name="displayName"><xsl:choose>
                    <xsl:when test="@name=''"><xsl:value-of select="$bundle/value[@key='facets.uknown']" /></xsl:when>
                    <xsl:when test="$facetname='document_type'">
                        <xsl:variable name="f"><xsl:value-of select="concat('fedora.model.', @name)" /></xsl:variable>
                        <xsl:choose>
                             <xsl:when test="$bundle/value[@key=$f]!=''"><xsl:value-of select="$bundle/value[@key=$f]" /></xsl:when>
                             <xsl:otherwise><xsl:value-of select="@name" /></xsl:otherwise>
                        </xsl:choose> (<xsl:value-of select="." />)
                    </xsl:when>
                    <xsl:when test="$facetname='dostupnost'">
                        <xsl:variable name="f"><xsl:value-of select="concat('dostupnost.', @name)" /></xsl:variable>
                        <xsl:if test="$policyPublic='false' or @name='public'" >
                            <xsl:value-of select="$bundle/value[@key=$f]" /> (<xsl:value-of select="." />)
                        </xsl:if>
                    </xsl:when>
                    <xsl:when test="$facetname='language'">
                        <xsl:variable name="f"><xsl:value-of select="concat('language.', @name)" /></xsl:variable>
                        <xsl:choose>
                             <xsl:when test="$bundle/value[@key=$f]!=''"><xsl:value-of select="$bundle/value[@key=$f]" /></xsl:when>
                             <xsl:otherwise><xsl:value-of select="@name" /></xsl:otherwise>
                        </xsl:choose> (<xsl:value-of select="." />)
                    </xsl:when>
                    <xsl:otherwise><xsl:value-of select="@name" /> (<xsl:value-of select="." />)</xsl:otherwise>
                </xsl:choose></xsl:variable>
                <xsl:if test="not (contains($fqVal, $fqId))">
                    <xsl:if test="position() = $numOpenedRows+1"><li class="more_facets">
                        <a><xsl:attribute name="href">javascript:toggleFacet('facet_<xsl:value-of select="$facetname" />')</xsl:attribute>...</a>
                    </li></xsl:if>
                    <li><xsl:if test="position() &gt; $numOpenedRows">
                        <xsl:attribute name="class">more_facets</xsl:attribute>
                        <xsl:attribute name="style">display:none;</xsl:attribute>
                    </xsl:if>
                    <a><xsl:attribute name="href">javascript:addFilter('<xsl:value-of select="$facetname" />', '<xsl:value-of select="@name" />')</xsl:attribute><xsl:value-of select="$displayName" /></a> 
                    </li>
                </xsl:if>
            </xsl:for-each>
            </ul>
        </li>
    </xsl:template>
</xsl:stylesheet>
