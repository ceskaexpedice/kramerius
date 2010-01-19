<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : test.xsl
    Created on : 17. prosinec 2009, 15:02
    Author     : Administrator
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" >
    <xsl:output method="html" omit-xml-declaration="" standalone="" />

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title>test.xsl</title>
            </head>
            <body xsl:exclude-result-prefixes="mods">
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
