<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version='1.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
>
    <xsl:output media-type="text/html; charset=UTF-8" encoding="UTF-8"/>
    <xsl:variable name="title" select="concat('Knihovna Václava Havla. (',response/result/@numFound,' dokumentů)')"/>
    <xsl:template match='/'>
        <html>
            <head>
                <title>
                    <xsl:value-of select="$title"/>
                </title>
                <xsl:call-template name="css"/>
            </head>
            <body>
                <div class="gVaclavhavel pg19591968" id="page">
                    <span class="pgcrn top clear">
                        <span class="rgt">
                            <span class="mid"> 
                            </span>
                        </span>
                    </span>
                    <div id="header">
                        <div class="logo">
                            <a class="txt" title="Knihovna Václava Havla" href="/">Knihovna Václava Havla
                            </a>
                            <a class="pic" title="Knihovna Václava Havla" href="/"> 
                            </a>
                        </div>
                        <div class="promo"/>
                        <div id="language">
                            <a title="English" class="emph" href="/en/">English
                            </a> |
                            <a title="Česky" href="/cs/">Česky
                            </a>
                        </div>
                        <div id="search">
                            <p class="title">Vyhledávání
                            </p>
                            <form id="frmSearch" method="get" action="select">
                                <input type="text" alt="Hledaný výraz" name="q" id="q">
									<xsl:attribute name="value"><xsl:for-each select="//str[@name='q']">
									<xsl:value-of select="." />
									</xsl:for-each></xsl:attribute>
									
								</input>
                                <input type="submit" title="Odeslat" alt=">" value="" name="submit" id="Ffs_submit"/>
                                <input type="hidden" value="xslt" name="wt" id="wt"/>
                                <input type="hidden" value="true" name="facet" id="facet"/>
                                <input type="hidden" value="autorNavigator" name="facet.field" id="facet.field"/>
                                <input type="hidden" value="DruhDokumentuNav" name="facet.field" id="facet.field"/>
                                <input type="hidden" value="kvh2.xsl" name="tr" id="tr"/>
                                <input type="hidden" value="1" name="facet.mincount" id="facet.mincount"/>
                                <div class="fBtns clear">
                                    <label title="Prohledat web KVH" id="todow">
                                        <input type="radio" checked="checked" value="web" id="Ffs_todow" name="todo"/> web
                                    </label>
                                    <label title="Prohledat archiv" id="todoa">
                                        <input type="radio" value="archive" id="Ffs_todoa" name="todo"/> archiv
                                    </label>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div class="clear" id="main">
                        <div id="article">
                            <div class="clear c0402" id="columnHolder">
                                <div class="pageCol" id="columnMiddle">
<h3><xsl:for-each select="//result"><xsl:value-of select="@numFound"/></xsl:for-each>&#160;dokumentů</h3>
                                    <xsl:apply-templates select="response/result/doc"/>
                                    <ul class="bottomlinks">
                                        <li>
                                            <a title="Zpět na úvodní stránku" href="/cs/index">Zpět na úvodní stránku
                                            </a>
                                        </li>
                                    </ul>
                                </div>
                                <div class="pageCol" id="columnRight">
                                    <h4>Fotogalerie</h4>
                                    <p class="subHead">Poznámka
                                    </p>
                                    <div class="box">
                                        <div class="content">
					Box content
                                        </div>
                                    </div>
                                    <p>Empty context</p>
                                </div>
                            </div>
                            <div class="clear" id="bottomBlock">
                                <p class="smallTitleTabs lftDarkTab titleFont">Fotogalerie
                                </p>
                                <div class="bBlock" id="bb1">
                                    <a title="" href="/" class="floatThumb">
                                        <img alt="" src=""/>
                                    </a>
                                    <h4>Title</h4>
                                    <p class="subHead">subtitle
                                    </p>
                                </div>
                                <div class="bBlock" id="bb2">
                                    <a title="" href="/" class="floatThumb">
                                        <img alt="" src=""/>
                                    </a>
                                    <h4>Title</h4>
                                    <p class="subHead">subtitle
                                    </p>
                                </div>
                                <div class="bBlock" id="bb3">
                                    <a title="" href="/" class="floatThumb">
                                        <img alt="" src=""/>
                                    </a>
                                    <h4>Title</h4>
                                    <p class="subHead">subtitle
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div class="clear" id="subNav">
                            <div class="homeLink">
                                <a title="Úvodní stránka" href="/cs/">
                                    <span>Úvodní stránka</span>
                                </a>
                            </div>
								<ul class="clear sNavLevel1" id="sNavUl">
									<li class="clear">
										<a title="Filtry" href="#" class="sn1 ">
											<span>Vyhledávání</span>
										</a>
										<xsl:if test="//arr[@name='fq'] or //str[@name='fq']">
										<ul class="clear sNavLevel2">
											<li class="clear">
												<a title="" href="#" class="sn2 ">
													<span>Pouzite filtry</span>
												</a>
												<ul class="sNavLevel3">
												<xsl:for-each select="//arr[@name='fq']">
													<li class="clear">
<a title=""  class="sn3 ">
														<xsl:attribute name="href">javascript:removeNavigation('<xsl:value-of select="."/>');</xsl:attribute>
															<span><xsl:value-of select="substring-before(.,':')"/>&#160;
															<xsl:value-of select="substring-after(.,':')"/></span>
														</a>
													</li>
												</xsl:for-each>
												<xsl:for-each select="//str[@name='fq']">
													<li class="clear">
														<a title="" class="sn3 ">
															<xsl:attribute name="href">javascript:removeNavigation('<xsl:value-of select="."/>');</xsl:attribute>
															<span><xsl:value-of select="substring-before(.,':')"/>&#160;
															<xsl:value-of select="substring-after(.,':')"/></span>
														</a>
													</li>
												</xsl:for-each>
												</ul>
											</li>
										</ul>
										</xsl:if>
									<xsl:apply-templates select="response/lst/lst/lst"/>
								</li>
							</ul>
                        </div>
                        <div id="aside">
                            <h4>Nepřehlédněte</h4>
                            <div id="dm1" class="dontMiss">
                                <h5>Havel v kostce</h5>
                                <p>
                                    <a title="Havel v kostce" href="/">
                                        <span class="pic"> 
                                        </span>
                                        <span class="desc">Stálá expozice v Galerii Montmartre
                                        </span>
                                        <span class="small">Podrobnosti
                                        </span>
                                    </a>
                                </p>
                            </div>
                            <div id="dm2" class="dontMiss">
                                <h5>E-shop</h5>
                                <p>
                                    <a title="E-shop" href="/">
                                        <span class="pic"> 
                                        </span>
                                        <span class="desc">Naše nabídka knih a DVD
                                        </span>
                                        <span class="small">Podrobnosti
                                        </span>
                                    </a>
                                </p>
                            </div>
                        </div>
                    </div>
                    <div id="nav">
                        <ul class="navTab clear">
                            <li>
                                <a title="Knihovna" href="/" class="emph">Knihovna
                                </a>
                            </li>
                            <li>
                                <a title="Archiv" href="/">Archiv
                                </a>
                            </li>
                        </ul>
                        <ul id="ulnav">
                            <li class="level0 nav1">
                                <a title="Úvod" class="aLevel0" href="/cs/index">Úvod
                                </a>
                                <ul>
                                    <li class="level1 nav1">
                                        <a title="Novinky" class="aLevel1" href="/cs/index/novinky">Novinky
                                        </a>
                                    </li>
                                    <li class="level1 nav2">
                                        <a title="Fotogalerie" class="aLevel1" href="/cs/index/fotogalerie">Fotogalerie
                                        </a>
                                    </li>
                                    <li class="level1 nav3">
                                        <a title="Kalendář akcí" class="aLevel1" href="/cs/index/kalendar">Kalendář akcí
                                        </a>
                                    </li>
                                </ul>
                            </li>
                            <li class="level0 nav2">
                                <a title="Václav Havel" class="emph aLevel0" href="/cs/vaclav-havel">Václav Havel
                                </a>
                                <ul>
                                    <li class="level1 nav1">
                                        <a title="Životopis" class="aLevel1" href="/cs/vaclav-havel/zivotopis">Životopis
                                        </a>
                                        <ul>
                                            <li class="level2 nav1">
                                                <a title="1936 – 1959 Dětství a mládí" class="aLevel2" href="/cs/vaclav-havel/zivotopis/1936-1959">1936 – 1959 Dětství a mládí
                                                </a>
                                            </li>
                                        </ul>
                                    </li>
                                    <li class="level1 nav2">
                                        <a title="Dílo" class="aLevel1" href="/cs/vaclav-havel/dilo">Dílo
                                        </a>
                                    </li>
                                    <li class="level1 nav3">
                                        <a title="Ocenění" class="aLevel1" href="/cs/vaclav-havel/oceneni">Ocenění
                                        </a>
                                    </li>
                                </ul>
                            </li>
                            <li class="level0 nav3">
                                <a title="O nás" class="aLevel0" href="/cs/o-nas">O nás
                                </a>
                                <ul>
                                    <li class="level1 nav1">
                                        <a title="Kdo jsme?" class="aLevel1" href="/cs/o-nas/kdo-jsme">Kdo jsme?
                                        </a>
                                    </li>
                                    <li class="level1 nav2">
                                        <a title="Tým knihovny" class="aLevel1" href="/cs/o-nas/tym-knihovny">Tým knihovny
                                        </a>
                                    </li>
                                    <li class="level1 nav3">
                                        <a title="Dokumenty" class="aLevel1" href="/cs/o-nas/ke-stazeni">Dokumenty
                                        </a>
                                    </li>
                                    <li class="level1 nav4">
                                        <a title="Tiskové ohlasy" class="aLevel1" href="/cs/o-nas/tiskove-ohlasy">Tiskové ohlasy
                                        </a>
                                    </li>
                                </ul>
                            </li>
                            <li class="level0 nav4">
                                <a title="Aktivity" class="aLevel0" href="/cs/aktivity">Aktivity
                                </a>
                                <ul>
                                    <li class="level1 nav1">
                                        <a title="Stálá expozice" class="aLevel1" href="/cs/aktivity/expozice">Stálá expozice
                                        </a>
                                    </li>
                                    <li class="level1 nav1">
                                        <a title="Pořádané akce" class="aLevel1" href="/cs/aktivity/akce">Pořádané akce
                                        </a>
                                    </li>
                                    <li class="level1 nav1">
                                        <a title="Publikace a výzkum" class="aLevel1" href="/cs/aktivity/publikace-vyzkum">Publikace a výzkum
                                        </a>
                                    </li>
                                    <li class="level1 nav1">
                                        <a title="Vzdělávací programy" class="aLevel1" href="/cs/aktivity/vzdelavaci-programy">Vzdělávací programy
                                        </a>
                                    </li>
                                    <li class="level1 nav1">
                                        <a title="E-shop" class="aLevel1" href="/cs/aktivity/shop">E-shop
                                        </a>
                                    </li>
                                </ul>
                            </li>
                            <li class="level0 nav5">
                                <a title="Podpora" class="aLevel0" href="/cs/podpora">Podpora
                                </a>
                                <ul>
                                    <li class="level1 nav1">
                                        <a title="Naši partneři" class="aLevel1" href="/cs/podpora/partneri">Naši partneři
                                        </a>
                                    </li>
                                    <li class="level1 nav1">
                                        <a title="Podpořte nás" class="aLevel1" href="/cs/podpora/podporte-nas">Podpořte nás
                                        </a>
                                    </li>
                                </ul>
                            </li>
                            <li class="level0 nav6">
                                <a title="Kontakty" class="aLevel0" href="/cs/kontakty">Kontakty
                                </a>
                                <ul>
                                    <li class="level1 nav1">
                                        <a title="Adresář" class="aLevel1" href="/cs/kontakty/adresar">Adresář
                                        </a>
                                    </li>
                                    <li class="level1 nav1">
                                        <a title="Otevírací doba" class="aLevel1" href="/cs/kontakty/oteviraci-doba">Otevírací doba
                                        </a>
                                    </li>
                                    <li class="level1 nav1">
                                        <a title="Pro média" class="aLevel1" href="/cs/kontakty/pro-media">Pro média
                                        </a>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                    </div>
                    <span class="pgcrn bot clear">
                        <span class="rgt">
                            <span class="mid"> 
                            </span>
                        </span>
                    </span>
                </div>
            </body>
        </html>
    </xsl:template>


    <xsl:template match="lst[@name='facet_fields']/*">
        <xsl:variable name="facet" select="@name"/>
		<ul class="clear sNavLevel2">
			<li class="clear">
				<a title="" href="#" class="sn2 ">
					<span><xsl:value-of select="@name"/></span>
				</a>
				<ul class="sNavLevel3">
					<xsl:apply-templates>
						<xsl:with-param name="facet">
							<xsl:value-of select="$facet"/>
						</xsl:with-param>
					</xsl:apply-templates>
				</ul>
			</li>
		</ul>
    </xsl:template>
    <xsl:template match="lst[@name='facet_fields']/lst/*">
	<xsl:param name="facet"></xsl:param>
		<li class="clear">
			<a title="" class="sn3 ">
				<xsl:attribute name="href">javascript:addNavigation('<xsl:value-of select="$facet"/>', '<xsl:value-of select="@name"/>');</xsl:attribute>
				<span><xsl:value-of select="@name"/>&#160;(<xsl:value-of select="."/>)</span>
			</a>
		</li>
    </xsl:template>

    <xsl:template match="doc">
        <xsl:variable name="pos" select="position()"/>
        <div class="doc">
            <dl class="bio">
                <dt>
                    <xsl:value-of select="./str[@name='nazev']"/>
                </dt>
				<dd>
					<xsl:apply-templates>
						<xsl:with-param name="pos">
							<xsl:value-of select="$pos"/>
						</xsl:with-param>
					</xsl:apply-templates>
				</dd>
            </dl>
        </div>
    </xsl:template>
    <xsl:template match="doc/*">
			<xsl:value-of select="@name"/>:&#160;
			<em>
				<xsl:value-of select="."/>
			</em>.<br/>
    </xsl:template>
    <xsl:template match="*"/>
    <xsl:template name="css">
        <link href="http://kvh2.2142.net/s_default.css" media="screen, projection" type="text/css" rel="stylesheet"/>
        <link href="http://kvh2.2142.net/s_handheld.css" media="handheld" type="text/css" rel="stylesheet"/>
        <link href="../media/styles/incad.css" type="text/css" rel="stylesheet"/>
        <!-- pro historii stranek -->
        <script type="text/javascript" src="../js/jquery-1.3.2.min.js"></script>
        <script type="text/javascript" src="../js/jquery.cookie.js"></script>
        <script type="text/javascript" src="../js/jquery.history.js"></script>
        <script type="text/javascript" src="../js/pageQuery.js"></script>
        <script type="text/javascript" src="../js/incad.js"></script>
    </xsl:template>
    <xsl:template match="*"/>
</xsl:stylesheet>
