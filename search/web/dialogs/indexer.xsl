<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:sp="http://www.w3.org/2001/sw/DataAccess/rf1/result"
version="1.0">
    <xsl:output method="html" encoding="UTF-8" />
    <xsl:param name="rows" select="'10'"/>
    <xsl:param name="offset" select="'0'"/>
    <xsl:param name="sort" select="'title'"/>
    <xsl:param name="sort_dir" select="'asc'"/>
    <xsl:param name="model" select="model"/>
    <xsl:template match="/">
        <table cellpadding="0" cellspacing="0" class="indexer_selected">
            <thead class="indexer_head"><tr>
                <td>
                    <xsl:if test="$sort = 'title'">
                        <xsl:if test="$sort_dir = 'asc'">
                            <a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', 0, 'title', 'desc')</xsl:attribute>title</a>
                            <span class="ui-icon indexer_order_down">title</span>
                        </xsl:if>
                        <xsl:if test="$sort_dir = 'desc'">
                            <a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', 0, 'title', 'asc')</xsl:attribute>title</a>
                            <span class="ui-icon indexer_order_up">title</span>
                        </xsl:if>
                    </xsl:if>
                    <xsl:if test="not($sort = 'title')">
                        <a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', 0, 'title', 'asc')</xsl:attribute>title</a>
                    </xsl:if>
                </td>
                <td>
                    <xsl:if test="$sort = 'date'">
                        <xsl:if test="$sort_dir = 'asc'">
                            <a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', 0, 'date', 'desc')</xsl:attribute>date</a>
                            <span class="ui-icon indexer_order_down">title</span>
                        </xsl:if>
                        <xsl:if test="$sort_dir = 'desc'">
                            <a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', 0, 'date', 'asc')</xsl:attribute>date</a>
                            <span class="ui-icon indexer_order_up">title</span>
                        </xsl:if>
                    </xsl:if>
                    <xsl:if test="not($sort = 'date')">
                        <a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', 0, 'date', 'desc')</xsl:attribute>date</a>
                    </xsl:if>
                </td></tr></thead>
        <xsl:for-each select="/sp:sparql/sp:results/sp:result">
            <xsl:variable name="title" select="normalize-space(./sp:title)" />
            <xsl:variable name="date" select="normalize-space(./sp:date)" />
            <tr class="indexer_result"><td>
            - 
            <a><xsl:attribute name="href">javascript:indexDoc('<xsl:value-of select="./sp:object/@uri" />', '<xsl:value-of select="$title" />');</xsl:attribute><xsl:value-of select="./sp:title" /></a>
            </td><td><xsl:value-of select="./sp:date" /></td>
            </tr>
        </xsl:for-each>
        <tr><td class="indexer_pager" colspan="2">
            <xsl:if test="$offset>0"><a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', <xsl:value-of select="$offset - $rows" />, '<xsl:value-of select="$sort" />', '<xsl:value-of select="$sort_dir" />')</xsl:attribute>previous</a></xsl:if>
            <xsl:if test="count(/sp:sparql/sp:results/sp:result)=$rows"><a><xsl:attribute name="href">javascript:loadFedoraDocuments('<xsl:value-of select="$model" />', <xsl:value-of select="$offset + $rows" />, '<xsl:value-of select="$sort" />', '<xsl:value-of select="$sort_dir" />')</xsl:attribute>next</a></xsl:if>
        </td></tr>
        </table>
    </xsl:template>

</xsl:stylesheet>
