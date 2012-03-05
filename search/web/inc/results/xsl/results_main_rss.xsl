<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

    <xsl:param name="applUrl" select="applUrl" />
    <xsl:param name="channelUrl" select="channelUrl" />
    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
   
<xsl:template match="/">
    <rss version="2.0">
  <channel> 
  <title><xsl:value-of select="$bundle/value[@key='application.title']"/></title> 
  <description><xsl:value-of select="$bundle/value[@key='search.results.title']"/></description> 
  <link>  <xsl:value-of select="$channelUrl" />   <xsl:value-of select="$bundle_url" />  </link> 
  
  <xsl:call-template name="docs" />
  </channel>
  </rss>
</xsl:template>


<xsl:template name="docs">
  <xsl:for-each select="//doc">  
    <xsl:variable name="pid" select="./str[@name='PID']"></xsl:variable>
    <xsl:variable name="title" select="./str[@name='dc.title']"></xsl:variable>
    <xsl:variable name="model_path" select="./arr[@name='model_path']/str/text()"></xsl:variable>
    <xsl:variable name="fmodel" ><xsl:value-of select="./str[@name='fedora.model']" /></xsl:variable>
    
    <item>
        <title><xsl:value-of select="$title" /></title>    
        <description>PID: <xsl:value-of select="$pid" /> Model: <xsl:value-of select="$bundle/value[@key=$fmodel]"/>        
        </description>
        <link><xsl:value-of select="$applUrl" />/handle/<xsl:value-of select="$pid" /></link>
        <guid><xsl:value-of select="$applUrl" />/handle/<xsl:value-of select="$pid" /></guid>
    </item>
  </xsl:for-each>  
</xsl:template>  

  
</xsl:stylesheet>