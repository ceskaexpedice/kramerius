<?xml version='1.0' encoding='UTF-8'?>

<xsl:stylesheet version='1.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
>

  <xsl:output media-type="text/html; charset=UTF-8" encoding="UTF-8"/> 
  
  <xsl:variable name="title" select="concat('Knihovna Václava Havla. (',response/result/@numFound,' dokumentů)')"/>
  
  <xsl:template match='/'>
    <html>
      <head>
        <title><xsl:value-of select="$title"/></title>
		<xsl:call-template name="css"/>
      </head>
      <body>
	  <div id="page">
        <div id="header"><xsl:call-template name="header"/></div>
        <ul id="mainmenu">
            <li class="right"><a href=""></a></li> 
        </ul>
        <div id="main">
            <div id="submenu"></div>
            <div id="main-in">
                <div id="content">
                    <div class="leftcol"><xsl:apply-templates select="response/lst/lst/lst"/></div>
                    <div class="rightcol"><xsl:apply-templates select="response/result/doc"/></div>
                </div>
                <hr class="cleaner" />
            </div>
        </div>
	   </div>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="lst">
    <xsl:variable name="facet" select="position()"/>
    <div class="navigace">
      <table width="100%">
        <xsl:apply-templates>
          <xsl:with-param name="facet"><xsl:value-of select="$facet"/></xsl:with-param>
        </xsl:apply-templates>
      </table>
    </div>
  </xsl:template>

   <xsl:template match="//lst/lst[@name='autorNavigator']/*">
    <tr>
      <td class="name">
        <xsl:value-of select="@name"/>
      </td>
      <td class="value">
        (<xsl:value-of select="."/>)
      </td>
    </tr>
  </xsl:template>


  <xsl:template match="doc">
    <xsl:variable name="pos" select="position()"/>
    <div class="doc">
      <table width="100%">
        <xsl:apply-templates>
          <xsl:with-param name="pos"><xsl:value-of select="$pos"/></xsl:with-param>
        </xsl:apply-templates>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="doc/arr" priority="100">
    <tr>
      <td class="name">
        <xsl:value-of select="@name"/>
      </td>
      <td class="value">
        <ul>
        <xsl:for-each select="*">
          <li><xsl:value-of select="."/></li>
        </xsl:for-each>
        </ul>
      </td>
    </tr>
  </xsl:template>


  <xsl:template match="doc/*">
    <tr>
      <td class="name">
        <xsl:value-of select="@name"/>
      </td>
      <td class="value">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="*"/>
  
  <xsl:template name="header">
    <h1><a href="/">Knihovna Václava Havla</a></h1>
	<div class="h-left">
		<a href="/"><img height="73" width="115" src="http://www.vaclavhavel-knihovna.org/media/images/logo.gif"/></a>
	</div>
	<div class="h-right">
		<form id="form-search" name="form-search" action="./">
			<input class="search-keyword" type="text" name="query" value=""/>
			<a class="search-submit" onclick="document.getElementById('form-search').submit(); return false;" href="#"><span class="hidden">Hledat</span></a>
			<input type="submit" class="hidden" value="HLEDAT"/>
			<input type="hidden" name="view" value="" />
			<input type="hidden" id="defaultNavigation" name="defaultNavigation" value="" />
		</form>
		<div id="langs">
			<span>
				<a href="/cs/">česky</a> | 
				<a href="/en/">english</a>
			</span>
		</div>
	</div>
  </xsl:template>

  <xsl:template name="css">
		<link href="http://kvh2.2142.net/s_default.css" media="screen, projection" type="text/css" rel="stylesheet"/>
		<link href="http://kvh2.2142.net/s_handheld.css" media="handheld" type="text/css" rel="stylesheet"/>
		<!--
		<link rel="stylesheet" href="../media/styles/main.css" type="text/css" media="screen, projection" />
		<link rel="stylesheet" href="../media/styles/main.css" type="text/css" media="screen, projection" />
		<link rel="stylesheet" href="../media/styles/home.css" type="text/css" media="screen, projection" />
		<link rel="stylesheet" href="../media/styles/global.css" type="text/css" media="screen, projection" />
		<link rel="stylesheet" href="../media/styles/print.css" type="text/css" media="print"/>
		<link rel="Shortcut Icon" type="image/ico" href="http://www.vaclavhavel-knihovna.org/media/images/favicon.ico" />
		<link rel="stylesheet" href="../media/styles/incad.css" type="text/css"/>
        <script type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
        <script type="text/javascript" src="js/pageQuery.js"></script>
		-->
        <!-- pro historii stranek -->
        <script type="text/javascript" src="js/jquery.history.js"></script>
        <script type="text/javascript" src="js/incad.js"></script>
  </xsl:template>

  <xsl:template match="*"/>

</xsl:stylesheet>
