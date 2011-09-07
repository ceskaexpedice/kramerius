<xsl:stylesheet  version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="no" encoding="UTF-8" omit-xml-declaration="yes" />
    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="dts" select="dts" />
    <xsl:template match="/">
        <xsl:value-of select="dts"/>
        <div><xsl:value-of select="$bundle/value[@key='home.document.type']"/>&#160;
        <xsl:for-each select="response/lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='document_type']/int">
            <xsl:variable name="tm">[<xsl:value-of select="@name" />]</xsl:variable>
            <xsl:if test="contains($dts, $tm)"><span><b><a>
                <xsl:attribute name="href">r.jsp?fq=document_type:<xsl:value-of select="@name" /></xsl:attribute>
                <xsl:attribute name="title">javascript:addNavigation('document_type', '<xsl:value-of select="@name" />')</xsl:attribute>
            <xsl:variable name="t"><xsl:choose>
            <xsl:when test=". = 1">document.type.<xsl:value-of select="@name" /></xsl:when>
            <xsl:when test=". &lt; 5">document.type.<xsl:value-of select="@name" />.2</xsl:when>
            <xsl:otherwise>document.type.<xsl:value-of select="@name" />.5</xsl:otherwise>
            </xsl:choose></xsl:variable>
            <xsl:value-of select="." />&#160;<xsl:value-of select="$bundle/value[@key=$t]"/></a></b></span>
            </xsl:if>
        </xsl:for-each></div>
        <script type="text/javascript">
        <xsl:comment><![CDATA[
        $(document).ready(function(){
            $('#dt_home>div>span').hide();
            rollTypes();
        });
        var rollIndex = 0;
        function rollTypes(){
            $('#dt_home>div>span:nth('+rollIndex+')').fadeOut(function(){
                rollIndex++;
                if(rollIndex>=$('#dt_home>div>span').length) rollIndex=0;
                $('#dt_home>div>span:nth('+rollIndex+')').show();
                setTimeout('rollTypes()', 4000);
            });
        }

        ]]> </xsl:comment>
        </script>
    </xsl:template>
</xsl:stylesheet>
