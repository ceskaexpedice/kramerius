
<xsl:stylesheet  version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:mods="http://www.loc.gov/mods/v3"
    exclude-result-prefixes="mods" >
    <xsl:output method="html" indent="yes" encoding="UTF-8"  omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="bundle_url" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="model" select="model" />
    <xsl:param name="level" select="level" />
    <xsl:template match="/">
        <xsl:apply-templates mode="info"/>
    </xsl:template>
    <xsl:template match="/mods:modsCollection/mods:mods" mode="info">
        <xsl:variable name="uuid" ><xsl:value-of select="./mods:identifier[@type='urn']"/></xsl:variable>
        <div id="details"><h2><xsl:value-of select="$bundle/value[@key=$model]"/></h2>
        <ul>
        
        <xsl:if test="./mods:identifier[@type='isbn']/text()">
        <li>
            <span class="label">ISBN: </span>
            <span class="value aleph" data-field="ISBN"><xsl:value-of select="./mods:identifier[@type='isbn']" /></span>
        </li>
        </xsl:if>
        
        <xsl:if test="./mods:identifier[@type='issn']/text()">
        <li>
            <span class="label">ISSN: </span>
            <span class="value aleph" data-field="ISSN"><xsl:value-of select="./mods:identifier[@type='issn']" /></span>
        </li>
        </xsl:if>
        <xsl:if test="./mods:identifier[@type='ccnb']/text()">
        <li>
            <span class="label">čČNB: </span>
            <span class="value aleph" data-field="ccnb"><xsl:value-of select="./mods:identifier[@type='ccnb']" /></span>
        </li>
        </xsl:if>
        <xsl:if test="mods:titleInfo/mods:title/text()">
        <li>
            <span class="label"><xsl:value-of select="$bundle/value[@key='filter.maintitle']"/></span>:&#160;
            <span class="value"><xsl:value-of select="mods:titleInfo/mods:title" /></span>
            
            <xsl:if test="mods:titleInfo/mods:subTitle/text()">
                <br/><span class="label"><xsl:value-of select="$bundle/value[@key='mods.subtitle']"/></span>:&#160;
                <span class="value"><xsl:value-of select="mods:titleInfo/mods:subTitle" /></span>
            </xsl:if>
        </li>
        </xsl:if>
        
        <xsl:if test="$level &gt; 0">
        </xsl:if>
        
        <xsl:if test="mods:part/mods:detail[@type = 'volume']/mods:number">
        <li>
        <span class="label">
        <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>&#160;
        </span>
        <span class="value">
        <xsl:value-of select="mods:part/mods:date" />&#160;
        </span>
        <span class="label">
        <xsl:value-of select="$bundle/value[@key='Číslo']"/>&#160;
        </span>
        <span class="value">
        <xsl:value-of select="mods:part/mods:detail[@type = 'volume']/mods:number" />
        </span>
        </li>
        </xsl:if>

        <xsl:if test="mods:part[@type = 'PeriodicalIssue']">
        <li>
        <span class="label">
        <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>&#160;
        </span>
        <span class="value">
        <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:date" />&#160;
        </span>
        <span class="label">
        <xsl:value-of select="$bundle/value[@key='Číslo']"/>&#160;
        </span>
        <span class="value">
        <xsl:value-of select="mods:part[@type = 'PeriodicalIssue']/mods:detail[@type = 'issue']/mods:number" />
        </span>
        </li>
        </xsl:if>

        <xsl:variable name="partType"><xsl:value-of select="mods:part/@type" /></xsl:variable>
        <!-- page -->
        <span class="value">
            <xsl:choose>
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
            </xsl:choose>
        </span>
        <!-- end page -->

        <!-- monograph unit -->
        <xsl:if test="mods:part[@type = 'Volume']/mods:detail">
        <span class="label">
        <xsl:value-of select="$bundle/value[@key='Volume']"/>
        </span>
        <span class="value">
        <xsl:if test="mods:part[@type = 'Volume']/mods:detail/mods:title != ''" >
            &#160;(<xsl:value-of select="mods:part[@type = 'Volume']/mods:detail/mods:title" />)
        </xsl:if> - <xsl:value-of select="mods:part[@type = 'Volume']/mods:detail/mods:number" />
        </span>
        </xsl:if>
        <!-- end monograph unit -->

        <!-- internal part -->
            <span class="label">
            <xsl:choose>
                <xsl:when test="$partType='Chapter'">
                    (<xsl:value-of select="$bundle/value[@key='Chapter']"/>) - </xsl:when>
                <xsl:when test="$partType='Table'">
                    (<xsl:value-of select="$bundle/value[@key='Table']"/>) - </xsl:when>
                <xsl:when test="$partType='Introduction'">
                    (<xsl:value-of select="$bundle/value[@key='Introduction']"/>) - </xsl:when>
            </xsl:choose>
            </span>
            <span class="value"><xsl:value-of select="mods:part/mods:extent/mods:list" /></span>
        <!-- end internal part -->


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
               <div>
               <span class="label">
                    <xsl:value-of select="./mods:role/mods:roleTerm[@type='text']" />:&#160;
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
                <div>
               <span class="label">
                    <xsl:value-of select="./mods:role/mods:roleTerm[@type='text']" />:&#160;
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
            <xsl:for-each select="mods:physicalDescription/mods:extent">
                <li>
                    <span class="value"><xsl:value-of select="." /></span>
                </li>
            </xsl:for-each>
            <!--
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
        -->
        </xsl:if>

        <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
        <h3><xsl:value-of select="$bundle/value[@key='Stav z hlediska ochrany fondů']"/></h3>
        <li>
            <span class="label">
                    <xsl:value-of select="$bundle/value[@key='Aktuální stav']"/>:
            </span>
            <span class="value">
                    <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
            </span>
        </li>
        </xsl:if>
        <xsl:if test="mods:location/mods:physicalLocation">
        <h3><xsl:value-of select="$bundle/value[@key='mods.physicalLocation']"/></h3>
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