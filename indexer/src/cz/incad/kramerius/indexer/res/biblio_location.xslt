<?xml version="1.1" encoding="UTF-8"?> 
<!-- 
Only for tranformation location mods element 
 -->
<xsl:stylesheet version="1.1"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
        xmlns:mods="http://www.loc.gov/mods/v3" 
         exclude-result-prefixes="mods"
    >
    <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" encoding="UTF-8" />

<xsl:template match="mods:location">
    <xsl:apply-templates select="mods:shelfLocator"/>
    <xsl:apply-templates select="mods:physicalLocation"/>
</xsl:template>


<xsl:template match="mods:shelfLocator">
  <field name="mods.shelfLocator">
     <xsl:value-of select="." />
 </field>
</xsl:template>

<xsl:template match="mods:physicalLocation">
  <field name="mods.physicalLocation">
       <xsl:value-of select="." />
  </field>
</xsl:template>

</xsl:stylesheet>