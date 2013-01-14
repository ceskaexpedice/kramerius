
<xsl:stylesheet  version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:mods="http://www.loc.gov/mods/v3"
    exclude-result-prefixes="mods" >
    <xsl:output method="html" indent="yes" encoding="UTF-8"  omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="model" select="model" />
    <xsl:template match="/">
        <xsl:apply-templates mode="info"/>
    </xsl:template>
    <xsl:template match="@*|node()" mode="xml" >
        <xsl:copy >
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/mods:modsCollection/mods:mods" mode="info">
        <xsl:variable name="uuid" ><xsl:value-of select="./mods:identifier[@type='urn']"/></xsl:variable>
            <xsl:choose>
                <xsl:when test="mods:titleInfo/mods:title">
                    <xsl:variable name="TITLE"><xsl:value-of select="mods:titleInfo/mods:title" /></xsl:variable>
                </xsl:when>
                <xsl:when test="mods:titleInfo/mods:title">
                    <xsl:variable name="TITLE"><xsl:value-of select="mods:titleInfo/mods:title" /></xsl:variable>
                </xsl:when>
            </xsl:choose>

            <div id="details"><ul>
            <xsl:attribute name="title" ><xsl:value-of select="mods:titleInfo/mods:title" /></xsl:attribute>
            <xsl:if test="./mods:identifier[@type='issn']/text()">
            <li>
                <span class="label">ISSN: </span>
                <span class="value"><xsl:value-of select="./mods:identifier[@type='issn']" /></span>
            </li>
            </xsl:if>
            <li class="value"><xsl:value-of select="mods:titleInfo/mods:title" /></li>
            <xsl:if test="mods:part/mods:detail[@type = 'volume']/mods:number">

            <li>
            <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>&#160;
            <xsl:value-of select="mods:part/mods:date" />&#160;
            <xsl:value-of select="$bundle/value[@key='Číslo']"/>&#160;
            <xsl:value-of select="mods:part/mods:detail[@type = 'volume']/mods:number" />
            </li>
            </xsl:if>

            <xsl:if test="mods:part[@type = 'PeriodicalIssue']">
            <li>
            <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>&#160;
            <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:date" />&#160;
            <xsl:value-of select="$bundle/value[@key='Číslo']"/>&#160;
            <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:detail[@type = 'issue']/mods:number" />
            </li>
            </xsl:if>

            <xsl:variable name="partType"><xsl:value-of select="mods:part/@type" /></xsl:variable>
            <xsl:variable name="partTypeLoc">mods.page.partType.<xsl:value-of select="mods:part/@type"/></xsl:variable>
            <!-- page -->
            <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;
            (<xsl:value-of select="$bundle/value[@key=$partTypeLoc]"/>)
            <!--xsl:choose>
                <xsl:when test="$partType='Blank'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;(<xsl:value-of select="$bundle/value[@key='Blank']"/>)</xsl:when>
                <xsl:when test="$partType='TitlePage'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;(<xsl:value-of select="$bundle/value[@key='TitlePage']"/>)</xsl:when>
                <xsl:when test="$partType='TableOfContents'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;(<xsl:value-of select="$bundle/value[@key='TableOfContents']"/>)</xsl:when>
                <xsl:when test="$partType='Index'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;(<xsl:value-of select="$bundle/value[@key='TableOfContents']"/>)</xsl:when>
                <xsl:when test="$partType='NormalPage'">
                    <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" /></xsl:when>
            </xsl:choose-->
            <!-- end page -->

            <!-- monograph unit -->
            <xsl:if test="mods:part[@type = 'Volume']/mods:detail">
            <xsl:value-of select="$bundle/value[@key='Volume']"/>
            <xsl:if test="mods:part[@type = 'Volume']/mods:detail/mods:title != ''" >
                &#160;(<xsl:value-of select="mods:part[@type = 'Volume']/mods:detail/mods:title" />)
            </xsl:if> - <xsl:value-of select="mods:part[@type = 'Volume']/mods:detail/mods:number" />
            </xsl:if>
            <!-- end monograph unit -->

            <!-- internal part -->
                <xsl:choose>
                    <xsl:when test="$partType='Chapter'">
                        (<xsl:value-of select="$bundle/value[@key='Chapter']"/>) -&#160;</xsl:when>
                    <xsl:when test="$partType='Table'">
                        (<xsl:value-of select="$bundle/value[@key='Table']"/>) -&#160;</xsl:when>
                    <xsl:when test="$partType='Introduction'">
                        (<xsl:value-of select="$bundle/value[@key='Introduction']"/>) -&#160;</xsl:when>
                </xsl:choose>
                <xsl:value-of select="mods:part/mods:extent/mods:list" />
            <!-- end internal part -->

        </ul>
            <ul>
            <li>
                <span class="label">ISSN: </span>
                <span class="value"><xsl:value-of select="./mods:identifier[@type='issn']" /></span>
            </li>
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='filter.maintitle']"/>: </span>
                <span class="value"><xsl:value-of select="mods:titleInfo/mods:title" /></span>
            </li>
            <xsl:if test="mods:titleInfo/mods:subTitle/text()">
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Podnázev']"/>: </span>
                <span class="value"><xsl:value-of select="mods:titleInfo/mods:subTitle" /></span>
            </li>
            </xsl:if>
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Druh dokumentu']"/>: </span>
                <span class="value"><xsl:value-of select="$bundle/value[@key=$model]"/></span>
            </li>


            <xsl:if test="mods:language/mods:languageTerm">
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='common.language']"/></span>:&#160;
                <span class="value"><xsl:for-each select="mods:language/mods:languageTerm">
                    <xsl:value-of select="." />&#160;
                </xsl:for-each></span>
            </li>
            </xsl:if>

            <xsl:if test="mods:name[@type='personal']/mods:role/mods:roleTerm[@type='code'] = 'cre'">
                <h3>
                    <xsl:value-of select="$bundle/value[@key='common.author']"/>
                </h3>
                <xsl:for-each select="mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'] = 'cre']"><li>
                    <xsl:variable name="roleTerm">mods.roleTerm.<xsl:value-of select="./mods:role/mods:roleTerm[@type='text']" /></xsl:variable>
                    <div>
                    <span class="label">
                         <xsl:value-of select="$bundle/value[@key=$roleTerm]"/>:&#160;
                    </span>
                   <span class="value">
                        <xsl:choose>
                            <xsl:when test="./mods:namePart[@type='family']">
                                 <xsl:value-of select="./mods:namePart[@type='family']" />,&#160;
                                 <xsl:for-each select="./mods:namePart[@type='given']"><xsl:value-of select="." />&#160;</xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="./mods:namePart[not(@type)]" />
                            </xsl:otherwise>
                        </xsl:choose> 
                   </span>
                    </div>
                </li></xsl:for-each>
            </xsl:if>

            <xsl:if test="mods:name[@type='personal']/mods:role/mods:roleTerm[@type='code'] = 'ctb'">
                <h3>
                    <xsl:value-of select="$bundle/value[@key='mods.contributor']"/>
                </h3>
                <xsl:for-each select="mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'] = 'ctb']"><li>
                   <xsl:variable name="roleTerm">mods.roleTerm.<xsl:value-of select="./mods:role/mods:roleTerm[@type='text']" /></xsl:variable>
                    <div>
                    <span class="label">
                         <xsl:value-of select="$bundle/value[@key=$roleTerm]"/>:&#160;
                    </span>
                   <span class="value">
                        <xsl:value-of select="./mods:namePart[@type='family']" />,&#160;
                        <xsl:for-each select="./mods:namePart[@type='given']"><xsl:value-of select="." />&#160;</xsl:for-each>
                   </span>
                   </div>
                </li></xsl:for-each>
            </xsl:if>

            <xsl:if test="count(mods:originInfo[@transliteration='publisher'])=1">
                <h3><xsl:value-of select="$bundle/value[@key='Vydavatel']"/></h3>
            </xsl:if>

            <xsl:if test="count(mods:originInfo[@transliteration='publisher'])>1">
                <h3><xsl:value-of select="$bundle/value[@key='Vydavatele']"/></h3>
            </xsl:if>
            <xsl:for-each select="mods:originInfo[@transliteration='publisher']">
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Název vydavatele']"/>: </span>
                <span class="value"><xsl:if test="./mods:publisher">
                     <xsl:value-of select="./mods:publisher" />
                </xsl:if></span>
            </li>
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Datum vydání']"/>: </span>
                <span class="value"><xsl:if test="./mods:dateIssued">
                      <xsl:value-of select="./mods:dateIssued" />
                </xsl:if></span>
            </li>
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Místo vydání']"/>: </span>
                <span class="value"><xsl:if test="./mods:place/mods:placeTerm">
                    <xsl:value-of select="./mods:place/mods:placeTerm" />
                    </xsl:if></span>
            </li>
            <h3></h3>
            </xsl:for-each>
            <xsl:if test="mods:originInfo[@transliteration='printer']">
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Název tiskaře']"/>: </span>
                <span class="value"><xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:publisher" /></span>
            </li>
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Místo tisku']"/>: </span>
                <span class="value"><xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:place/mods:placeTerm" /></span>
            </li>
            </xsl:if>

            <xsl:if test="mods:physicalDescription/mods:extent">
                <h3><xsl:value-of select="$bundle/value[@key='Fyzický popis']"/></h3>
                <xsl:choose>
                <xsl:when test="contains(mods:physicalDescription/mods:extent, ',')">
                    <li>
                        <span class="label"><xsl:value-of select="$bundle/value[@key='Rozměry']"/>: </span>
                        <span class="value"><xsl:value-of select="substring-after(mods:physicalDescription/mods:extent, ',')" /></span>
                    </li>
                    <li>
                        <span class="label"><xsl:value-of select="$bundle/value[@key='Rozsah']"/>: </span>
                        <span class="value"><xsl:value-of select="substring-before(mods:physicalDescription/mods:extent, ',')" /></span>
                    </li>
                </xsl:when>
                <xsl:otherwise>
                    <li>
                        <span class="label"><xsl:value-of select="$bundle/value[@key='Rozsah']"/>: </span>
                        <span class="value"><xsl:value-of select="mods:physicalDescription/mods:extent" /></span>
                    </li>
                </xsl:otherwise>
            </xsl:choose>
            </xsl:if>

            <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
            <h3><xsl:value-of select="$bundle/value[@key='Stav z hlediska ochrany fondů']"/></h3>
            <li>
                <span class="value">
                        <xsl:value-of select="$bundle/value[@key='Aktuální stav']"/>:
                        <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
                </span>
            </li>
            </xsl:if>
            <xsl:if test="mods:location/mods:physicalLocation">
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Místo uložení']"/>: </span>
                <span class="value"><xsl:value-of select="mods:location/mods:physicalLocation" /></span>
            </li>
            </xsl:if>
            <xsl:if test="mods:location/mods:shelfLocator">
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='common.signature']"/>: </span>
                <span class="value"><xsl:value-of select="mods:location/mods:shelfLocator" /></span>
            </li>
            </xsl:if>

            <xsl:if test="mods:originInfo/mods:frequency">
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Periodicita']"/>: </span>
                <span class="value"><xsl:value-of select="mods:originInfo/mods:frequency" /></span>
            </li>
            </xsl:if>
            <xsl:if test="mods:part/mods:detail[@type='regularsupplement']">
            <li>
                <span class="label"><xsl:value-of select="$bundle/value[@key='Pravidelná příloha']"/>: </span>
                <span class="value"><xsl:value-of select="mods:part/mods:detail[@type='regularsupplement']" /></span>
            </li>
            </xsl:if>

            <xsl:if test="mods:physicalDescription/mods:note[@type!='preservationStateOfArt']/text()">
            <h3><xsl:value-of select="$bundle/value[@key='Poznámky']"/></h3>
            <li>
                <span class="value"><xsl:value-of select="mods:physicalDescription/mods:note" /></span>
            </li>
            </xsl:if>

            </ul>
        </div>

    </xsl:template>
</xsl:stylesheet>