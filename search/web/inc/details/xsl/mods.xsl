
<xsl:stylesheet  version="1.0"
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                 xmlns:mods="http://www.loc.gov/mods/v3"
                 exclude-result-prefixes="mods" >
    <xsl:output method="html" indent="yes" encoding="UTF-8"  omit-xml-declaration="yes" />

    <xsl:param name="bundle_url" select="'http://localhost:8080/search/i18n?action=bundle&amp;lang=cs&amp;name=labels'" />
    <xsl:param name="bundle" select="document($bundle_url)/bundle" />
    <xsl:param name="model" select="model" />
    <xsl:param name="level" select="level" />
    <xsl:template match="/">
        
        <head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /></head>
        <xsl:apply-templates mode="info"/>
    </xsl:template>
    
    <xsl:template name="common">
        
        <xsl:variable name="uuid" >
            <xsl:choose>
                <xsl:when test="./mods:identifier[@type='uuid']"><xsl:value-of select="./mods:identifier[@type='uuid']"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="./mods:identifier[@type='urn']"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
                <xsl:if test="./mods:identifier[@type='isbn']/text()">
                    <li>
                        <span class="label">ISBN: </span>
                        <span class="value aleph" data-field="ISBN">
                            <xsl:value-of select="./mods:identifier[@type='isbn']" />
                        </span>
                    </li>
                </xsl:if>
        
                <xsl:if test="./mods:identifier[@type='issn']/text()">
                    <li>
                        <span class="label">ISSN: </span>
                        <span class="value aleph" data-field="ISSN">
                            <xsl:value-of select="./mods:identifier[@type='issn']" />
                        </span>
                    </li>
                </xsl:if>
                <xsl:if test="./mods:identifier[@type='ccnb']/text()">
                    <li>
                        <span class="label">čČNB: </span>
                        <span class="value aleph" data-field="ccnb">
                            <xsl:value-of select="./mods:identifier[@type='ccnb']" />
                        </span>
                    </li>
                </xsl:if>
                <xsl:if test="mods:titleInfo/mods:title/text()">
                    <li>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='filter.maintitle']"/>
                        </span>:&#160;
                        <span class="value">
                            <xsl:value-of select="mods:titleInfo/mods:nonSort" />&#160;
                            <xsl:value-of select="mods:titleInfo/mods:title" />
                        </span>
            
                        <xsl:if test="mods:titleInfo/mods:subTitle/text()">
                            <br/>
                            <span class="label">
                                <xsl:value-of select="$bundle/value[@key='mods.subtitle']"/>
                            </span>:&#160;
                            <span class="value">
                                <xsl:value-of select="mods:titleInfo/mods:subTitle" />
                            </span>
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

                <xsl:if test="mods:language/mods:languageTerm">
                    <li>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='common.language']"/>
                        </span>:&#160;
                        <span class="value">
                            <xsl:for-each select="mods:language/mods:languageTerm">
                                <xsl:value-of select="." />&#160;
                            </xsl:for-each>
                        </span>
                    </li>
                </xsl:if>
                <xsl:variable name="roleTermCode" select="mods:name[@type='personal']/mods:role/mods:roleTerm[@type='code']"/>
                <xsl:if test="($roleTermCode='cre') or ($roleTermCode='aut')">
                    <li>
                    <h4>
                        <xsl:value-of select="$bundle/value[@key='common.author']"/>
                    </h4>
                    <xsl:for-each select="mods:name[@type='personal']">
                        <xsl:if test="./mods:role/mods:roleTerm[@type='code'] = 'cre' or ./mods:role/mods:roleTerm[@type='code'] = 'aut'">
                        
                            <xsl:variable name="roleTerm">mods.roleTerm.<xsl:value-of select="./mods:role/mods:roleTerm[@type='text']" /></xsl:variable>
                            <div>
                                <span class="label">
                                    <xsl:value-of select="$bundle/value[@key=$roleTerm]"/>:&#160;
                                </span>
                                <span class="value">
                                    <xsl:choose>
                                        <xsl:when test="./mods:namePart[@type='family']">
                                            <xsl:value-of select="./mods:namePart[@type='family']" />,&#160;
                                            <xsl:for-each select="./mods:namePart[@type='given']">
                                                <xsl:value-of select="." />&#160;
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="./mods:namePart[not(@type)]" />
                                        </xsl:otherwise>
                                    </xsl:choose> 
                                </span>
                            </div>
                        
                        </xsl:if>
                    </xsl:for-each>
                    </li>
                </xsl:if>

                <xsl:if test="mods:name[@type='personal']/mods:role/mods:roleTerm[@type='code'] = 'ctb'">
                        <li>
                    <h4>
                        <xsl:value-of select="$bundle/value[@key='mods.contributor']"/>
                    </h4>
                    <xsl:for-each select="mods:name[@type='personal'][mods:role/mods:roleTerm[@type='code'] = 'ctb']">
                            <xsl:variable name="roleTerm">mods.roleTerm.<xsl:value-of select="./mods:role/mods:roleTerm[@type='text']" /></xsl:variable>
                            <div>
                                <span class="label">
                                    <xsl:value-of select="$bundle/value[@key=$roleTerm]"/>:&#160;
                                </span>
                                <span class="value">
                                    <xsl:choose>
                                        <xsl:when test="./mods:namePart[@type='family']">
                                            <xsl:value-of select="./mods:namePart[@type='family']" />,&#160;
                                            <xsl:for-each select="./mods:namePart[@type='given']">
                                                <xsl:value-of select="." />&#160;
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="./mods:namePart[not(@type)]" />
                                        </xsl:otherwise>
                                    </xsl:choose> 
                                </span>
                            </div>
                    </xsl:for-each>
                        </li>
                </xsl:if>
                
                <xsl:if test="count(mods:originInfo[@transliteration='publisher']) &gt; 1">
                    <li>
                    <xsl:if test="count(mods:originInfo[@transliteration='publisher'])=1">
                        <h4>
                            <xsl:value-of select="$bundle/value[@key='Vydavatel']"/>
                        </h4>
                    </xsl:if>

                    <xsl:if test="count(mods:originInfo[@transliteration='publisher'])>1">
                        <h4>
                            <xsl:value-of select="$bundle/value[@key='Vydavatele']"/>
                        </h4>
                    </xsl:if>
                    <xsl:for-each select="mods:originInfo[@transliteration='publisher']">
                        <div>
                            <span class="label">
                                <xsl:value-of select="$bundle/value[@key='Název vydavatele']"/>: </span>
                            <span class="value">
                                <xsl:if test="./mods:publisher">
                                    <xsl:value-of select="./mods:publisher" />
                                </xsl:if>
                            </span>
                        </div>
                        <div>
                            <span class="label">
                                <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>: </span>
                            <span class="value">
                                <xsl:if test="./mods:dateIssued">
                                    <xsl:value-of select="./mods:dateIssued" />
                                </xsl:if>
                            </span>
                        </div>
                        <div>
                            <span class="label">
                                <xsl:value-of select="$bundle/value[@key='Místo vydání']"/>: </span>
                            <span class="value">
                                <xsl:if test="./mods:place/mods:placeTerm">
                                    <xsl:value-of select="./mods:place/mods:placeTerm" />
                                </xsl:if>
                            </span>
                        </div>
                    </xsl:for-each>
                    </li>
                </xsl:if>
                <xsl:if test="mods:originInfo[@transliteration='printer']">
                    <li>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='Název tiskaře']"/>: </span>
                        <span class="value">
                            <xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:publisher" />
                        </span>
                    </li>
                    <li>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='Místo tisku']"/>: </span>
                        <span class="value">
                            <xsl:value-of select="mods:originInfo[@transliteration='printer']/mods:place/mods:placeTerm" />
                        </span>
                    </li>
                </xsl:if>

                <xsl:if test="mods:physicalDescription/mods:extent">
                        <li>
                    <h4>
                        <xsl:value-of select="$bundle/value[@key='Fyzický popis']"/>
                    </h4>
                    <xsl:for-each select="mods:physicalDescription/mods:extent">
                        <div>
                        <span class="value">
                                <xsl:value-of select="." />
                            </span>
                        </div>   
                    </xsl:for-each>
                        </li>
                </xsl:if>

                <xsl:if test="mods:physicalDescription/mods:note[@type='preservationStateOfArt']">
                    <li>
                    <h4>
                        <xsl:value-of select="$bundle/value[@key='Stav z hlediska ochrany fondů']"/>
                    </h4>
                    <div>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='Aktuální stav']"/>:
                        </span>
                        <span class="value">
                            <xsl:value-of select="mods:physicalDescription/mods:note[@type='preservationStateOfArt']" />
                        </span>
                    </div>
                    </li>
                </xsl:if>
                <xsl:if test="mods:location/mods:physicalLocation">
                    <li>
                    <h4>
                        <xsl:value-of select="$bundle/value[@key='mods.physicalLocation']"/>
                    </h4>
                    <div>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='Místo uložení']"/>: </span>
                        <span class="value">
                            <xsl:value-of select="mods:location/mods:physicalLocation" />
                        </span>
                    </div>
                    </li>
                </xsl:if>
                <xsl:if test="mods:location/mods:shelfLocator">
                    <li>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='common.signature']"/>: </span>
                        <span class="value">
                            <xsl:value-of select="mods:location/mods:shelfLocator" />
                        </span>
                    </li>
                </xsl:if>

                <xsl:if test="mods:originInfo/mods:frequency">
                    <li>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='Periodicita']"/>: </span>
                        <span class="value">
                            <xsl:value-of select="mods:originInfo/mods:frequency" />
                        </span>
                    </li>
                </xsl:if>
                <xsl:if test="mods:part/mods:detail[@type='regularsupplement']">
                    <li>
                        <span class="label">
                            <xsl:value-of select="$bundle/value[@key='Pravidelná příloha']"/>: </span>
                        <span class="value">
                            <xsl:value-of select="mods:part/mods:detail[@type='regularsupplement']" />
                        </span>
                    </li>
                </xsl:if>

                <xsl:if test="mods:physicalDescription/mods:note[@type!='preservationStateOfArt']/text()">
                    <li>
                    <h4>
                        <xsl:value-of select="$bundle/value[@key='Poznámky']"/>
                    </h4>
                    <div>
                        <span class="value">
                            <xsl:value-of select="mods:physicalDescription/mods:note" />
                        </span>
                    </div>
                    </li>
                </xsl:if>
    </xsl:template>
    
    <xsl:template name="monograph">
        <li>
            
        </li>
    </xsl:template>
    
    <xsl:template name="periodical">
        
    </xsl:template>
    
    <xsl:template name="monographunit">
        <li>
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
        </li>
    </xsl:template>
    
    <xsl:template name="periodicalvolume">
        <li>
            <div><span class="label">
                genre: 
            </span>
            <span class="value">
                <xsl:value-of select="./mods:genre" />
            </span>
            </div>
            <div>
                <span class="value">
                    <xsl:value-of select="./mods:partNumber" />
                </span>
            </div>
            
            <xsl:if test="./mods:originInfo/mods:dateIssued">
            <div>
                <span class="label">
                    <xsl:value-of select="$bundle/value[@key='Datum vydání']"/>: </span>
                <span class="value">
                        <xsl:value-of select="./mods:originInfo/mods:dateIssued" />
                </span>
            </div>
            </xsl:if>
        </li>
    </xsl:template>
    
    <xsl:template name="periodicalitem">
        
    </xsl:template>
    
    <xsl:template name="internalpart">

        <xsl:variable name="partType">
            <xsl:value-of select="mods:part/@type" />
        </xsl:variable>
        <li>
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
            <span class="value">
                <xsl:value-of select="mods:part/mods:extent/mods:list" />
            </span>
         </li>
    </xsl:template>
    
    <xsl:template name="page">
        <xsl:variable name="partType">
            <xsl:value-of select="mods:part/@type" />
        </xsl:variable>
        <xsl:variable name="partTypeLocStr">mods.page.partType.<xsl:value-of select="mods:part/@type"/></xsl:variable>
        <xsl:variable name="partTypeLoc" select="$bundle/value[@key=$partTypeLocStr]"/>

        <li><span class="value">
            <xsl:value-of select="mods:part/mods:detail[@type = 'pageNumber']/mods:number" />&#160;
            <xsl:if test="$partTypeLoc != ''">(<xsl:value-of select="$partTypeLoc"/>)</xsl:if>
        </span></li>
    </xsl:template>
    
    <xsl:template match="/mods:modsCollection/mods:mods" mode="info">
        
        <div id="details">
            <h2>
                <xsl:value-of select="$bundle/value[@key=$model]"/>
            </h2>
            
            <h3><xsl:value-of select="mods:titleInfo[not(@type)]/mods:title"/></h3>
            <ul>
            <xsl:call-template name="common" />
            <xsl:choose>
                <xsl:when test="$model='monograph'">
                    <xsl:call-template name="monograph" />
                </xsl:when>
                <xsl:when test="$model='periodical'">
                    <xsl:call-template name="periodical" />
                </xsl:when>
                <xsl:when test="$model='monographunit'">
                    <xsl:call-template name="monographunit" />
                </xsl:when>
                <xsl:when test="$model='periodicalvolume'">
                    <xsl:call-template name="periodicalvolume" />
                </xsl:when>
                <xsl:when test="$model='periodicalitem'">
                    <xsl:call-template name="periodicalitem" />
                </xsl:when>
                <xsl:when test="$model='internalpart'">
                    <xsl:call-template name="internalpart" />
                </xsl:when>
                <xsl:when test="$model='page'">
                    <xsl:call-template name="page" />
                </xsl:when>
            </xsl:choose>
            </ul>
        </div>
    </xsl:template>
</xsl:stylesheet>